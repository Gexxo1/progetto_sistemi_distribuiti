package com.progetto_sistemi_distribuiti.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration //Indica a Spring che questa classe contiene definizioni di configurazione. All'avvio dell'applicazione, Spring leggerà questa classe per creare e configurare i "Bean" (oggetti gestiti dal framework)
@EnableMethodSecurity //permette di usare annotazioni come @PreAuthorize("hasRole('ADMIN')") direttamente sui metodi del Controller
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll() //Chiunque (anche gli utenti non loggati) può fare richieste di login. Altrimenti i guest non potrebbero neanche loggare
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll() //Qualsiasi guest può effettuare richieste GET (lettura) a /api/products/*
                .anyRequest().authenticated() //Tutto il resto delle richieste richiedono login
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //è Stateless --> non memorizzerà lo stato dell'utente
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}