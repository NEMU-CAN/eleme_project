package com.iteleme.backend.service.impl;

import com.iteleme.backend.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Set<String> revokedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            revokedTokens.add(token);
        }
    }

    @Override
    public boolean isRevoked(String token) {
        return token != null && revokedTokens.contains(token);
    }
}
