package com.nexacro.fullstack.runner.boot8.service;

import com.nexacro.fullstack.runner.boot8.config.RelayProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the HTTP passthrough relay to an upstream EXIM exchange service.
 *
 * <p>Writes the response body directly to {@link HttpServletResponse#getOutputStream()}
 * to bypass Spring's HttpMessageConverter selection, which on Spring Boot 2.7
 * would otherwise base64-wrap a {@code byte[]} body whenever the response
 * Content-Type ends up being {@code application/json} (Jackson's default
 * byte[] encoding). Direct stream write keeps the body as raw bytes regardless
 * of declared content type — the only mode nexacro can parse uniformly.
 */
@Service
public class RelayService {

    private static final Logger LOG = LoggerFactory.getLogger(RelayService.class);

    private final RestTemplate relayRestTemplate;
    private final RelayProperties props;

    public RelayService(@Qualifier("relayRestTemplate") RestTemplate relayRestTemplate,
                        RelayProperties props) {
        this.relayRestTemplate = relayRestTemplate;
        this.props = props;
    }

    /**
     * Relay an inbound multipart request to the configured upstream URL,
     * writing the upstream (or fallback) bytes directly into {@code resp}.
     */
    public void relay(MultipartHttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (props.getUrl() == null || props.getUrl().trim().isEmpty()) {
            LOG.warn("[RelayService] nexacro.relay.exim.url is not configured — returning 503");
            writeJsonError(resp, 503, -1, "EXIM relay url not configured");
            return;
        }

        // Build parts map
        MultiValueMap<String, HttpEntity<?>> parts = new LinkedMultiValueMap<>();

        // Add form parameters
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String paramName = entry.getKey();
            for (String value : entry.getValue()) {
                parts.add(paramName, new HttpEntity<>(value, new HttpHeaders()));
            }
        }

        // Add file parts
        for (Map.Entry<String, MultipartFile> entry : req.getFileMap().entrySet()) {
            MultipartFile mf = entry.getValue();
            try {
                byte[] bytes = mf.getBytes();
                final String originalFilename = mf.getOriginalFilename();
                ByteArrayResource resource = new ByteArrayResource(bytes) {
                    @Override
                    public String getFilename() {
                        return originalFilename;
                    }
                };
                HttpHeaders fileHeaders = new HttpHeaders();
                String ct = mf.getContentType();
                fileHeaders.setContentType(MediaType.parseMediaType(
                        ct != null ? ct : "application/octet-stream"));
                parts.add(mf.getName(), new HttpEntity<>(resource, fileHeaders));
            } catch (IOException e) {
                LOG.error("[RelayService] Failed to read file part '{}': {}", mf.getName(), e.getMessage());
                writeJsonError(resp, 503, -2, "EXIM upstream call failed: " + e.getMessage());
                return;
            }
        }

        String partNames = parts.keySet().stream().collect(Collectors.joining(", "));
        LOG.info("[RelayService] Relaying {} part(s) [{}] to URL: {}",
                parts.size(), partNames, props.getUrl());

        // Build outbound headers
        HttpHeaders out = new HttpHeaders();
        out.setContentType(MediaType.MULTIPART_FORM_DATA);
        for (Map.Entry<String, String> header : props.getForwardHeaders().entrySet()) {
            out.add(header.getKey(), header.getValue());
        }

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> entity = new HttpEntity<>(parts, out);

        try {
            ResponseEntity<byte[]> upstream =
                    relayRestTemplate.exchange(props.getUrl(), HttpMethod.POST, entity, byte[].class);

            LOG.info("[RelayService] Upstream responded with status: {}", upstream.getStatusCode());

            MediaType upstreamContentType = upstream.getHeaders().getContentType();
            String contentType = (upstreamContentType != null
                    ? upstreamContentType.toString()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE);
            List<String> disposition = upstream.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION);

            resp.setStatus(upstream.getStatusCodeValue());
            resp.setContentType(contentType);
            if (disposition != null) {
                for (String d : disposition) {
                    resp.addHeader(HttpHeaders.CONTENT_DISPOSITION, d);
                }
            }
            byte[] body = upstream.getBody();
            if (body != null) {
                resp.setContentLength(body.length);
                resp.getOutputStream().write(body);
                resp.getOutputStream().flush();
            }
        } catch (RestClientException e) {
            LOG.error("[RelayService] Upstream call failed: {}", e.getMessage());
            writeJsonError(resp, 503, -2, "EXIM upstream call failed: " + e.getMessage());
        }
    }

    private void writeJsonError(HttpServletResponse resp, int status, int errorCode, String errorMsg) throws IOException {
        String json = String.format(
                "{\"version\":\"1.0\",\"Parameters\":[{\"id\":\"ErrorCode\",\"value\":%d},{\"id\":\"ErrorMsg\",\"value\":\"%s\"}],\"Datasets\":[]}",
                errorCode, errorMsg.replace("\"", "\\\""));
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setContentLength(body.length);
        resp.getOutputStream().write(body);
        resp.getOutputStream().flush();
    }
}
