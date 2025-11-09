package army.helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// ApplicationContextAware를 구현하여 ApplicationContext를 사용할 수 있도록 합니다.
@EnableScheduling
@ConfigurationPropertiesScan
@EnableWebSecurity
@SpringBootApplication(scanBasePackages = "army.helper")
@EnableJpaAuditing
public class EnlistKoreaApplication implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(EnlistKoreaApplication.class, args);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		EnlistKoreaApplication.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}