package com.numpty.framework.security.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class JwtProvider {

    private final SecretKey secretKey;
    private final long validityInMilliseconds;

    public JwtProvider(String jwtSecret, long validityInMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String createToken(String subject, Map<String, Object> claims) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date(now))
            .expiration(new Date(now + validityInMilliseconds))
            .signWith(secretKey)
            .compact();
    }

    public Claims validateAndParseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
