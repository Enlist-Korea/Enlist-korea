package com.militarysupport.recruit_helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RecruitHelperApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecruitHelperApplication.class, args);
	}

}
