package com.example.mogakserver.auth.application.service;

import com.example.mogakserver.common.exception.model.UnAuthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.mogakserver.common.exception.enums.ErrorCode.TOKEN_TIME_EXPIRED_EXCEPTION;

@Service
@RequiredArgsConstructor
public class TokenService {
    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new UnAuthorizedException(TOKEN_TIME_EXPIRED_EXCEPTION);
    }
}