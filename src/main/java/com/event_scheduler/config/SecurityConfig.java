package com.event_scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.event_scheduler.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // get username,password,role UserDetails{}
    // convert raw password -> hashed password BcryptPassword()
    // compare passwords
    // return true or false acess granted/denied
    // which urls to protect configured in SecurityFilterChain
    // which roles to allow configured in authorizeHttpRequests

    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/admin/**").authenticated()
                        .requestMatchers("/hello").authenticated()
                        .anyRequest()
                        .authenticated())
                .httpBasic();
        System.out.println("SecurityFilterChain: " + " customUserDetailsService: "
                + customUserDetailsService.loadUserByUsername("pawan@gmail.com"));
        // UserDetails:-> [Username=pawan@gmail.com, Password=[PROTECTED],Enabled=false,
        // AccountNonExpired=true, CredentialsNonExpired=true, AccountNonLocked=true,
        // Granted Authorities=[ROLE_ADMIN]]

        return http.build(); // on role based auth branch
    }

    // hashing raw password, to compare with hashed password in DB.
    // @Autowired
    // public void configure(AuthenticationManagerBuilder auth) throws Exception {
    // auth.authenticationProvider(authenticationProvider());
    // }

    // @Bean
    // public DaoAuthenticationProvider authenticationProvider(){
    // DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    // authProvider.setUserDetailsService(customUserDetailsService);
    // authProvider.setPasswordEncoder(passwordEncoder);

    // return authProvider;
    // }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}