package com.learnfl.dto.content;

import lombok.Data;

/** 单词 */
@Data
public class WordVO {

    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    private String wordType;
}
