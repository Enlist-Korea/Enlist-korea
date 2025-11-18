package army.helper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@CrossOrigin(origins = "*")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            // 1. 요청에서 Origin 헤더를 가져옵니다.
            String origin = request.getHeader("Origin");

            List<String> staticAllowedOrigins = (List.of(
                    "http://localhost:5173",
                    "http://localhost:5177",
                    "https://oppositional-atonalistic-shawn.ngrok-free.dev" // <- 여기 추가
            ));

            if (origin != null && (
                    // 로컬 네트워크 IP 대역 (192.168.x.x, 172.16.x.x, 10.x.x.x 등)으로 시작하면
                    origin.startsWith("http://192.168.") ||
                            origin.startsWith("http://172.16.") ||
                            origin.startsWith("http://10.")
            )) {
                // allowCredentials=true를 유지하기 위해, Origin을 정확히 하나만 설정합니다.
                config.setAllowedOrigins(List.of(origin));
            } else {
                // 그 외의 경우 (localhost, ngrok 등)는 정적 목록을 사용합니다.
                config.setAllowedOrigins(staticAllowedOrigins);
            }

            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return config;
        };
    }
}