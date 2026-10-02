package com.wallet.controller;

import com.wallet.dto.AuthResponse;
import com.wallet.dto.CreateUserAccountRequest;
import com.wallet.dto.CreateUserAccountResponse;
import com.wallet.dto.LoginRequest;
import com.wallet.model.User;
import com.wallet.repo.UserRepo;
import com.wallet.service.JwtService;
import com.wallet.service.ServiceCall;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ServiceCall walletService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo users;
    private final JwtService jwtService;

    public AuthController(ServiceCall walletService, AuthenticationManager authenticationManager, UserRepo users, JwtService jwtService) {
        this.walletService = walletService;
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserAccountResponse register(@Valid @RequestBody CreateUserAccountRequest request) {
        return walletService.createUserAndAccount(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = users.findByEmail(request.getEmail()).orElseThrow();
        return new AuthResponse(jwtService.createToken(user), "Bearer", jwtService.getExpirationSeconds());
    }
}
