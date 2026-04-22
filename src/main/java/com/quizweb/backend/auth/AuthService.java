package com.quizweb.backend.auth;

import com.quizweb.backend.auth.dto.LoginRequest;
import com.quizweb.backend.auth.dto.LoginResponse;
import com.quizweb.backend.auth.dto.RegisterRequest;
import com.quizweb.backend.auth.dto.RegisterResponse;
import com.quizweb.backend.common.exception.ConflictException;
import com.quizweb.backend.user.UserAccount;
import com.quizweb.backend.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpirationMs;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMs,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpirationMs = jwtExpirationMs;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(authentication.getName());
        return new LoginResponse(token, "Bearer", jwtExpirationMs);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Username already taken");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole("USER");
        userAccountRepository.save(account);

        return new RegisterResponse("Registered successfully", username);
    }
}
