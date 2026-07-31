package com.learnfl.dto.practice;

import lombok.Data;

/** 练习词项（word 模式 text=单词本身；sentence 模式 text=英文句子） */
@Data
public class PracticeItemVO {

    private Long id;
    /** 需要打的内容 */
    private String text;
    /** 音标（句子模式可为空） */
    private String phonetic;
    /** 中文释义 */
    private String meaning;
    /** 额外信息：单词词性 / 句子日文 */
    private String extra;
}
