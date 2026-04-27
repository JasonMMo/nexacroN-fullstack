package com.nexacro.fullstack.runner.boot17.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * External system relay controller — contract endpoint #14.
 *
 * POST /uiadapter/relay/exim_exchange.do
 * Accepts multipart/form-data, returns application/octet-stream.
 * Stub implementation: logs inbound part names and returns RELAY_STUB_OK.
 */
@RestController
@RequestMapping("/uiadapter")
public class RelayController {

    /**
     * Endpoint #14 — External system relay (exim exchange).
     *
     * Consumes multipart/form-data; produces application/octet-stream.
     * Stub body: logs part names and returns a small fixed payload.
     */
    @PostMapping(
            value = "/relay/exim_exchange.do",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> relayEximExchange(MultipartHttpServletRequest request) {
        Enumeration<String> partNames = request.getParameterNames();
        while (partNames.hasMoreElements()) {
            System.out.println("[RelayController] param: " + partNames.nextElement());
        }
        for (String fileName : request.getFileMap().keySet()) {
            System.out.println("[RelayController] file part: " + fileName);
        }
        byte[] body = "RELAY_STUB_OK".getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }
}
