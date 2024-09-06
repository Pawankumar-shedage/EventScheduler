package com.event_scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf->csrf.disable())
            .cors(cors->cors.disable())
            .authorizeHttpRequests((authz) -> authz
                 .requestMatchers("/user/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }

}