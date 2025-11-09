plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.militarysupport"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("mysql:mysql-connector-java:8.0.33")

//	implementation("org.springframework.boot:spring-boot-starter-webflux") // WebClient 제공
	implementation("org.springframework:spring-webflux")
	implementation("io.projectreactor.netty:reactor-netty-http")


	implementation("org.jsoup:jsoup:1.17.2") // 크롤링을 위한 HTML 파싱 및 데이터추출

	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")

	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
	implementation("jakarta.xml.bind:jakarta.xml.bind-api")
	implementation("org.glassfish.jaxb:jaxb-runtime")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
