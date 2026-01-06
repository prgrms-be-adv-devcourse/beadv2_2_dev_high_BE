package com.dev_high.user.auth.jwt;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JwtProvider {
    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
                      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(String userId, Set<String> roles){
        return generateToken(userId, roles, accessTokenExpiration);
    }

    public String generateRefreshToken(String userId, Set<String> roles){
        return generateToken(userId, roles, refreshTokenExpiration);
    }


    public String generateToken(String userId,
                                Set<String> roles,
                                long expirationTime){
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setSubject(userId)
                .claim("roles", roles)
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

