package com.learnfl.dto.practice;

import lombok.Data;

/** 练习词项（word 模式 text=单词本身；sentence 模式 text=当前练习语言的句子） */
@Data
public class PracticeItemVO {

    private Long id;
    /** 需要打的内容 */
    private String text;
    /** 音标（句子模式可为空） */
    private String phonetic;
    /** 中文释义 */
    private String meaning;
    /** 句子原始英文翻译（与日语句子的罗马音分开传递） */
    private String english;
    /** 句子原始中文翻译 */
    private String chinese;
    /** 句子原始日文 */
    private String japanese;
    /** 额外信息：单词词性 / 句子日文 */
    private String extra;
    /** 切分单元列表（句子模式已切分时），null 表示未切分 */
    private java.util.List<java.util.Map<String, String>> segments;
}
