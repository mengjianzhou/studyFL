package com.learnfl.dto.manage;

import lombok.Data;

/** 新增/修改句子 */
@Data
public class SentenceSaveRequest {

    private String english;
    private String chinese;
    private String japanese;
    private String sentenceType;
}
