package com.event_scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.event_scheduler.model.User;

@Configuration
public class SecurityConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf->csrf.disable())
            .cors(cors->cors.disable())
            .authorizeHttpRequests((authz) -> authz
                 .requestMatchers("/user/**").hasAnyRole("USER","ADMIN")
                //  .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
                // .anyRequest().authenticated()
            );
        return http.build();
    }

    // for api testing
    // @Bean
    // public UserDetailsService userDetailsService() {
    //     UserDetails user = User.withUsername("user")
    //         .password(passwordEncoder().encode("password"))
    //         .roles("USER")
    //         .build();

    //     UserDetails admin = User.withUsername("admin")
    //         .password(passwordEncoder().encode("admin123"))
    //         .roles("ADMIN")
    //         .build();

    //     return new InMemoryUserDetailsManager(user, admin);
    // }

}