package com.nexacro.uiadapter.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

/**
 * Relay service contract — proxies a multipart inbound request to an
 * upstream EXIM exchange. The implementation writes the response body
 * directly to {@link HttpServletResponse#getOutputStream()} for raw-byte
 * fidelity.
 */
public interface RelayService {

    void relay(MultipartHttpServletRequest req, HttpServletResponse resp) throws IOException;
}
