package com.event_scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // get username and password        UserDetails{}
    // convert raw password -> hashed password      BcryptPassword()
    // compare passwords
    // return true or false acess granted/denied
    // which urls to protect    configured in SecurityFilterChain
    // which roles to allow    configured in authorizeHttpRequests
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest()
                        .authenticated());
        return http.build();
        // on role based auth.
    }

}