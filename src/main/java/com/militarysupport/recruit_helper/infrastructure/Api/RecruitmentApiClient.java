package com.militarysupport.recruit_helper.infrastructure.Api;

import com.militarysupport.recruit_helper.config.MilitaryApiProperties;
import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.domain.Requirement;
import com.militarysupport.recruit_helper.dto.RequirementApiDto;
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
                    .block();
        } catch (WebClientException e) {
            log.warn("Failed to fetch currently open recruitments: {}", e.getMessage());
            return Collections.emptyList(); // 오류 발생 시 빈 리스트 반환
        }
    }

    // 모집 조건을 동기적으로 조회합니다.
    public List<RequirementApiDto> fetchRecruitmentRequirements() {
        try {
            return requirementWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", properties.requirement().serviceKey())
                            .queryParam("someParam", "value")
                            .build())
                    .retrieve()
                    // [수정] bodyToFlux의 타입을 RequirementApiDto.class로 변경합니다.
                    .bodyToFlux(RequirementApiDto.class)
                    .collectList()
                    .block();
        } catch (WebClientException e) {
            log.warn("Failed to fetch recruitment requirements: {}", e.getMessage());
            // [수정] 반환 타입을 List<RequirementApiDto>에 맞게 변경합니다.
            return Collections.emptyList();
        }
    }
}