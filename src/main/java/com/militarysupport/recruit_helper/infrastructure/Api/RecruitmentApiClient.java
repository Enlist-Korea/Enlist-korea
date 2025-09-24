package com.militarysupport.recruit_helper.infrastructure.Api;

import com.militarysupport.recruit_helper.config.MilitaryApiProperties;
import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.domain.Requirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class RecruitmentApiClient {

    private final MilitaryApiProperties properties;
    private final WebClient statusWebClient;
    private final WebClient requirementWebClient;

    public RecruitmentApiClient(
            MilitaryApiProperties properties,
            @Qualifier("statusWebClient") WebClient statusWebClient,
            @Qualifier("requirementWebClient") WebClient requirementWebClient) {
        this.properties = properties;
        this.statusWebClient = statusWebClient;
        this.requirementWebClient = requirementWebClient;
    }

    // 모집 현황을 동기적으로 조회합니다.
    public List<Recruitment> fetchCurrentlyOpenRecruitments() {
        try {
            return statusWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", properties.status().serviceKey())
                            .queryParam("someParam", "value")
                            .build())
                    .retrieve()
                    .bodyToFlux(Recruitment.class)
                    .collectList()
                    .block(); // <-- Mono 결과를 동기적으로 기다립니다.
        } catch (WebClientException e) {
            log.warn("Failed to fetch currently open recruitments: {}", e.getMessage());
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }
    }

    // 모집 조건을 동기적으로 조회합니다.
    public List<Requirement> fetchRecruitmentRequirements() {
        try {
            return requirementWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", properties.requirement().serviceKey())
                            .queryParam("someParam", "value")
                            .build())
                    .retrieve()
                    .bodyToFlux(Requirement.class)
                    .collectList()
                    .block(); // <-- Mono 결과를 동기적으로 기다립니다.
        } catch (WebClientException e) {
            log.warn("Failed to fetch recruitment requirements: {}", e.getMessage());
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }
    }
}