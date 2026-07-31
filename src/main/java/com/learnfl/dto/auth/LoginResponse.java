package com.learnfl.dto.auth;

import lombok.Data;

/** 登录/注册响应 */
@Data
public class LoginResponse {

    private String token;
    private UserVO user;

    public LoginResponse(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}
