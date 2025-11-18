plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.militarysupport"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 웹 API (컨트롤러용)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA (Repository 인터페이스 때문에 필요)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 롬복 (엔티티/DTO에 @Getter, @Builder 등 쓴 경우)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 임시로 H2 메모리 DB (실제 DB 연결 안 해도 일단 서버는 뜸)
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
