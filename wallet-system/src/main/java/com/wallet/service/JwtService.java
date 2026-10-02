package com.wallet.service;

import com.wallet.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationSeconds;

    public JwtService(@Value("${wallet.jwt.secret}") String secret,
                      @Value("${wallet.jwt.expiration-seconds:3600}") long expirationSeconds) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getEmail()).claim("role", user.getRole())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(key).compact();
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public long getExpirationSeconds() { return expirationSeconds; }
}
