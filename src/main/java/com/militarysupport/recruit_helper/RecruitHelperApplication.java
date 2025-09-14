package com.militarysupport.recruit_helper;

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
