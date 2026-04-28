package com.nexacro.fullstack.runner.boot8.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the EXIM relay endpoint (#14).
 * Bound from {@code nexacro.relay.exim.*} in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "nexacro.relay.exim")
public class RelayProperties {

    /** Target upstream URL. Empty/null means relay is disabled (503 fallback). */
    private String url;

    /** TCP connect timeout in milliseconds. */
    private int connectTimeout = 5000;

    /** Socket read timeout in milliseconds. */
    private int readTimeout = 30000;

    /** Additional HTTP headers to forward to the upstream service. */
    private Map<String, String> forwardHeaders = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Map<String, String> getForwardHeaders() {
        return forwardHeaders;
    }

    public void setForwardHeaders(Map<String, String> forwardHeaders) {
        this.forwardHeaders = forwardHeaders;
    }
}
