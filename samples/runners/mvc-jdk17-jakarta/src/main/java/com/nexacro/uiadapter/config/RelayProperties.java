package com.nexacro.uiadapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RelayProperties {

    @Value("${nexacro.relay.exim.url:}")
    private String url;

    @Value("${nexacro.relay.exim.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${nexacro.relay.exim.read-timeout:30000}")
    private int readTimeout;

    private Map<String,String> forwardHeaders = new HashMap<>();

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int v) { this.connectTimeout = v; }
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int v) { this.readTimeout = v; }
    public Map<String,String> getForwardHeaders() { return forwardHeaders; }
    public void setForwardHeaders(Map<String,String> v) { this.forwardHeaders = v; }
}
