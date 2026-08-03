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
    /** 切分单元列表（已解析），null 表示未切分 */
    private java.util.List<java.util.Map<String, String>> segments;
}
