package army.helper.infrastructure;

import army.helper.config.ApiProperties;
import army.helper.domain.Recruitment;
import army.helper.dto.RequirementApiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

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
            @Qualifier("requirementWebClient") WebClient requirementWebClient){
        this.apiProperties = properties;
        this.statusWebClient = statusWebClient;
        this.requirementWebClient = requirementWebClient;
    }

    public List<Recruitment> fetchCurrentRecruitments(){
        try{
            return statusWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", apiProperties.status().serviceKey())
                            .queryParam("someParam", "value")
                            .build())
                    .retrieve()
                    .bodyToFlux(Recruitment.class)
                    .collectList()
                    .block();
        } catch(WebClientException e){
            log.warn("Failed to fetch currently open recruitments :{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<RequirementApiDto> fetchCurrentRecruitRequirements(){
        try{
            return requirementWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", apiProperties.requirement().serviceKey())
                            .queryParam("someParam", "value")
                            .build())
                    .retrieve()
                    .bodyToFlux(RequirementApiDto.class)
                    .collectList()
                    .block();
        } catch(WebClientException e){
            log.warn("Failed to fetch recruitment requirements :{}", e.getMessage());
            return Collections.emptyList();
        }
    }


}
