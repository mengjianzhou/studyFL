package com.learnfl.dto.content;

import lombok.Data;

/** 句子 */
@Data
public class SentenceVO {

    private Long id;
    private String english;
    private String chinese;
    private String japanese;
    private String sentenceType;
}
