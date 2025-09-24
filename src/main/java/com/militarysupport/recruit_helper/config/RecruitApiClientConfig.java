package com.militarysupport.recruit_helper.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class RecruitApiClientConfig {

    @Bean
    public WebClient statusWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://apis.data.go.kr/1300000/mmaStatus")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(10))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                ))
                .build();
    }

    @Bean
    public WebClient requirementWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://apis.data.go.kr/1300000/mmaRequirement")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofSeconds(10))
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                ))
                .build();
    }
}
