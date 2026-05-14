package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.service.RelayService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

/**
 * EXIM relay controller — multipart in, octet-stream out.
 *
 * <p>The path/consumes/produces tuple must NOT change (contract). Method
 * returns {@code void} — the {@link RelayService} writes the response body
 * directly to {@link HttpServletResponse#getOutputStream()} to preserve
 * raw-byte fidelity regardless of negotiated Content-Type.
 */
@Controller
@RequiredArgsConstructor
public class RelayController {

    private final RelayService relayService;

    @PostMapping(
            value = "/relay/exim_exchange.do",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void relayEximExchange(MultipartHttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        relayService.relay(request, response);
    }
}
