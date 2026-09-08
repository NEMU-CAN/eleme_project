package com.iteleme.backend.service;

import java.util.List;

public interface AccessService {
    void ensureBusinessOwner(Integer businessId);

    List<Integer> ownedBusinessIds(Integer userId);
}
