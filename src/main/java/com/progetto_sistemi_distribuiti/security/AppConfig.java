package com.progetto_sistemi_distribuiti.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

// Applica il DP Role-based access control
// I criteri di sicurezza sono definiti in SecurityConfig.java
@Configuration
public class AppConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("mario").password("{noop}password").roles("UTENTE").build();
        UserDetails reviewer = User.withUsername("luigi").password("{noop}password").roles("REVISORE").build();
        UserDetails admin = User.withUsername("prof").password("{noop}password").roles("AMMINISTRATORE", "REVISORE").build();
        return new InMemoryUserDetailsManager(user, reviewer, admin);
    }
}
