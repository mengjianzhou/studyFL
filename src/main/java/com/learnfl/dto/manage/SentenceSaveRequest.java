package com.learnfl.dto.manage;

import lombok.Data;

/** 新增/修改句子 */
@Data
public class SentenceSaveRequest {

    private String english;
    private String chinese;
    private String japanese;
    private String sentenceType;
    /** 切分单元 JSON 字符串，null 表示不修改切分 */
    private String segmentsJson;
}
