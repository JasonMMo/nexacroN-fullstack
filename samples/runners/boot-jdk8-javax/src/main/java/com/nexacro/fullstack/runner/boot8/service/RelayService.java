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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the HTTP passthrough relay to an upstream EXIM exchange service.
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
     * Relay an inbound multipart request to the configured upstream URL.
     *
     * @param req the inbound multipart servlet request
     * @return the upstream response (or a 503 nexacro-envelope JSON on failure)
     */
    public ResponseEntity<byte[]> relay(MultipartHttpServletRequest req) {
        if (props.getUrl() == null || props.getUrl().trim().isEmpty()) {
            LOG.warn("[RelayService] nexacro.relay.exim.url is not configured — returning 503");
            return buildJsonError(503, -1, "EXIM relay url not configured");
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
                return buildJsonError(503, -2, "EXIM upstream call failed: " + e.getMessage());
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

            // Build downstream response headers
            HttpHeaders down = new HttpHeaders();
            MediaType upstreamContentType = upstream.getHeaders().getContentType();
            down.setContentType(upstreamContentType != null
                    ? upstreamContentType
                    : MediaType.APPLICATION_OCTET_STREAM);
            String disposition = upstream.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
            if (disposition != null) {
                down.add(HttpHeaders.CONTENT_DISPOSITION, disposition);
            }

            return ResponseEntity.status(upstream.getStatusCode())
                    .headers(down)
                    .body(upstream.getBody());

        } catch (RestClientException e) {
            LOG.error("[RelayService] Upstream call failed: {}", e.getMessage());
            return buildJsonError(503, -2, "EXIM upstream call failed: " + e.getMessage());
        }
    }

    private ResponseEntity<byte[]> buildJsonError(int status, int errorCode, String errorMsg) {
        String json = String.format(
                "{\"version\":\"1.0\",\"Parameters\":[{\"id\":\"ErrorCode\",\"value\":%d},{\"id\":\"ErrorMsg\",\"value\":\"%s\"}],\"Datasets\":[]}",
                errorCode, errorMsg.replace("\"", "\\\""));
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.status(status).headers(headers).body(body);
    }
}
