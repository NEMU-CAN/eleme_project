package com.iteleme.backend.context;

import com.iteleme.backend.exception.UnauthorizedException;

public final class CurrentUserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new UnauthorizedException("请先登录");
        }
        return user;
    }

    public static Integer userId() {
        return require().userId();
    }

    public static Integer role() {
        return require().role();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
