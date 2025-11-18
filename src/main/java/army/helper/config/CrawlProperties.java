package army.helper.config;

import army.helper.dto.CrawlTarget;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "crawling")
@Component
@Getter
@Setter
public class CrawlProperties {
    private List<CrawlTarget> targets = new ArrayList<>();
}