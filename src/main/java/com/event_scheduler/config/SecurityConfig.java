package com.event_scheduler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.event_scheduler.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // get username and password UserDetails{}
    // convert raw password -> hashed password BcryptPassword()
    // compare passwords
    // return true or false acess granted/denied
    // which urls to protect configured in SecurityFilterChain
    // which roles to allow configured in authorizeHttpRequests

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(@Lazy CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        // .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest()
                        .authenticated()
                        )
                .httpBasic(Customizer.withDefaults());

        System.out.println("SecurityFilterChain: "+" customUserDetailsService: "+customUserDetailsService.toString());
        return http.build();
        // on role based auth.
    }

    // hasing raw password, to compare with hashed password in DB.
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

    // username,password validation.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}