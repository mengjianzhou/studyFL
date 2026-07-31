package com.learnfl.dto.auth;

import lombok.Data;

/** 更新用户资料 */
@Data
public class UpdateUserRequest {

    private String nickname;
    private Long activeLanguageId;
}
