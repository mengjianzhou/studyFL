package com.learnfl.dto.manage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 新增/修改单词 */
@Data
public class WordSaveRequest {

    @NotBlank(message = "单词不能为空")
    private String word;

    private String phonetic;
    private String meaning;
    private String wordType;
}
