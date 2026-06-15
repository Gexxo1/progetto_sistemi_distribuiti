package com.progetto_sistemi_distribuiti.security;

// serve a contenere i dati della richiesta di login (Utilizzato in AuthController come parametro)
public record AuthRequest(String username, String password) {}
