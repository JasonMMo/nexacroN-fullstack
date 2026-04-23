package com.nexacro.fullstack.runner.boot17.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class WebConfig {

    /**
     * Primary ObjectMapper bean — matches EnvelopeCodec's config so inbound
     * envelope bodies round-trip identically.
     */
    @Bean
    public ObjectMapper nexacroObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    @Bean
    public MappingJackson2HttpMessageConverter nexacroJsonConverter(ObjectMapper nexacroObjectMapper) {
        return new MappingJackson2HttpMessageConverter(nexacroObjectMapper);
    }
}
