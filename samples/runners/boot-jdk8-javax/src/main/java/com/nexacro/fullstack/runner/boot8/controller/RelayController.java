package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.runner.boot8.service.RelayService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * External system relay controller — contract endpoint #14.
 *
 * POST /uiadapter/relay/exim_exchange.do
 * Accepts multipart/form-data, returns application/octet-stream.
 * Delegates all logic to {@link RelayService}.
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
    public ResponseEntity<byte[]> relayEximExchange(MultipartHttpServletRequest request) {
        return relayService.relay(request);
    }
}
