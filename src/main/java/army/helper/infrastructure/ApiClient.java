package army.helper.infrastructure;

import army.helper.config.ApiProperties;
import army.helper.dto.recruitment_status.RecruitmentStatusListResponse;
import army.helper.dto.recruitment_status.RecruitmentStatusResponse;
import army.helper.dto.recruitment_status.RequirementApiDto;
import army.helper.dto.recruitment_status.RequirementListResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ApiClient {

    private final ApiProperties apiProperties;
    private final WebClient statusWebClient;
    private final WebClient requirementWebClient;

    public ApiClient(
            ApiProperties properties,
            @Qualifier("statusWebClient") WebClient statusWebClient,
            @Qualifier("requirementWebClient") WebClient requirementWebClient
    ) {
        this.apiProperties = properties;
        this.statusWebClient = statusWebClient;
        this.requirementWebClient = requirementWebClient;
    }

    /**
     * 🪖 모집 현황 API (XML → DTO)
     */
    public List<RecruitmentStatusResponse> fetchCurrentRecruitments() {
        String url = String.format(
                "%s/list?serviceKey=%s&pageNo=100&numOfRows=100",
                apiProperties.status().baseUrl(),
                apiProperties.status().serviceKey()
        );

        try {
            log.info("📡 Requesting Recruitment API: {}", url);

            RecruitmentStatusListResponse response = statusWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RecruitmentStatusListResponse.class)
                    .block();

            if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
                log.warn("Recruitment API returned empty or malformed body");
                return Collections.emptyList();
            }

            List<RecruitmentStatusResponse> items = response.getBody().getItems();
            log.info("✅ Recruitment API parsed {} items", items.size());
            return items;

        } catch (WebClientResponseException e) {
            log.warn("⚠️ Recruitment API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("❌ Recruitment API failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }


    }

    /**
     * 📘 필수 요건(학과/자격증) API (JSON or XML)
     */
    public List<RequirementApiDto> fetchCurrentRecruitRequirements() {
        String url = String.format(
                "%s/list?serviceKey=%s&pageNo=1&numOfRows=10",
                apiProperties.requirement().baseUrl(),
                apiProperties.requirement().serviceKey()
        );

        try {
            log.info("📡 Requesting Requirement API: {}", url);

            RequirementListResponse response = requirementWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RequirementListResponse.class)
                    .block();

            List<RequirementApiDto> items = Optional.ofNullable(response)
                    .map(RequirementListResponse::getBody)
                    .map(RequirementListResponse.Body::getItems)
                    .orElse(Collections.emptyList());

            if (items.isEmpty()) {
                log.warn("⚠️ Requirement API returned empty items");
            } else {
                log.info("✅ Requirement API parsed {} items", items.size());
            }

            return items;

        } catch (WebClientResponseException e) {
            log.warn("⚠️ Requirement API error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("❌ Requirement API failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}