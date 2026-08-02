package com.learnfl.dto.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 翻译请求 */
@Data
public class TranslateRequest {

    @NotBlank(message = "翻译文本不能为空")
    private String text;

    /** ja / en / zh */
    private String from = "ja";

    /** ja / en / zh */
    private String to = "en";
}
