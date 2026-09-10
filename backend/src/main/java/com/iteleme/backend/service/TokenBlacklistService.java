package com.iteleme.backend.service;

public interface TokenBlacklistService {
    void revoke(String token);

    boolean isRevoked(String token);
}
