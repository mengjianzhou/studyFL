package com.learnfl.common;

/** 当前登录用户上下文（ThreadLocal，由 JwtInterceptor 填充） */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USERNAME = new ThreadLocal<>();

    public static void set(Long userId, String username) {
        CURRENT_USER_ID.set(userId);
        CURRENT_USERNAME.set(username);
    }

    public static Long userId() {
        Long id = CURRENT_USER_ID.get();
        if (id == null) {
            throw new BizException(401, "未登录");
        }
        return id;
    }

    public static String username() {
        return CURRENT_USERNAME.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USERNAME.remove();
    }
}
