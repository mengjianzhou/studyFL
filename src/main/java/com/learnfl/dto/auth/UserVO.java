package com.learnfl.dto.auth;

import lombok.Data;

/** 用户信息 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Long activeLanguageId;

    public UserVO(Long id, String username, String nickname, String avatar, Long activeLanguageId) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.activeLanguageId = activeLanguageId;
    }
}
