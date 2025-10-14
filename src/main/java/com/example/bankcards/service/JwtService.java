package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    <T> T extractClaim(String token, Function<Claims, T> resolver);
    String extractUsername(String token);
    boolean isValidAccess(String token, UserDetails user);
    boolean isValidRefresh(String token, User user);

}
