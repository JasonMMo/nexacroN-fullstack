package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.runner.boot17.service.RelayService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

/**
 * External system relay controller — contract endpoint #14.
 *
 * POST /uiadapter/relay/exim_exchange.do
 * Accepts multipart/form-data, returns application/octet-stream.
 * Delegates all logic to {@link RelayService}, which writes the response
 * body directly to {@link HttpServletResponse#getOutputStream()} to ensure
 * raw-byte fidelity regardless of negotiated Content-Type.
 */
@RestController
@RequestMapping("/uiadapter")
public class RelayController {

    private final RelayService relayService;

    public RelayController(RelayService relayService) {
        this.relayService = relayService;
    }

    /**
     * Endpoint #14 — External system relay (exim exchange).
     * Path/consumes/produces must NOT be changed (rules §2 contract).
     */
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
