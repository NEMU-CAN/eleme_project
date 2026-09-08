package com.iteleme.backend.support;

public final class RequestValues {
    private RequestValues() {
    }

    public static <T> T first(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }
}
