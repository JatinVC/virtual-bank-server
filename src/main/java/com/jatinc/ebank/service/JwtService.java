package com.jatinc.ebank.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    public String JWT_SECRET;

    public String extractUserKey(String token){
        return extractClaim(token, claims -> claims.get("userToken", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        return true;
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .setSigningKey(JWT_SECRET.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
