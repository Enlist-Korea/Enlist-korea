package com.militarysupport.recruit_helper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "military.api")
public record MilitaryApiProperties(
        Api status,
        Api requirement
) {
    public record Api(String baseUrl, String serviceKey, int timeoutMs) {}
}