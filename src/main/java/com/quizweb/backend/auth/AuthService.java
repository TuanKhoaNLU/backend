package com.quizweb.backend.auth;

import com.quizweb.backend.auth.dto.LoginRequest;
import com.quizweb.backend.auth.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpirationMs;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(request.username());
        return new LoginResponse(token, "Bearer", jwtExpirationMs);
    }
}
