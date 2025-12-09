package com.dev_high.apigateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    private final String secretKey;
    private final long accessTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {

        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
    }


    public String generateAccessToken(String userId, String role){
        return generateToken(userId, role, accessTokenExpiration);
    }

    public String generateToken(String userId,
                                String role,
                                long expirationTime){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}

