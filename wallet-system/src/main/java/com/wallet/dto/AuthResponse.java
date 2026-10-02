package com.wallet.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) { }
