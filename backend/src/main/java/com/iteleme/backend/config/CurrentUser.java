package com.iteleme.backend.config;

// ============================================================
// [阶段① 新增] 当前登录用户上下文（ThreadLocal）
// 说明：由 AuthInterceptor 在请求进入时写入 token 解析出的 userId，
//       请求结束后清除。控制器用 CurrentUser.resolve(路径userId) 取身份：
//       token 优先，无 token 回落路径 userId（兼容旧前端）。
// ============================================================
public final class CurrentUser {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CurrentUser() {
    }

    /** 写入当前请求的用户编号（可为 null）。 */
    public static void set(String userId) {
        HOLDER.set(userId);
    }

    /** 读取当前请求的用户编号；无则返回 null。 */
    public static String get() {
        return HOLDER.get();
    }

    /** 解析身份：token 里的 userId 优先，否则用传入的 fallback（路径 userId）。 */
    public static String resolve(String fallback) {
        String current = HOLDER.get();
        return current != null ? current : fallback;
    }

    /** 请求结束后清除，避免线程复用泄漏。 */
    public static void clear() {
        HOLDER.remove();
    }
}
