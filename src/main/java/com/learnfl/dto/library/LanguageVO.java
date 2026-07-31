package com.learnfl.dto.library;

import lombok.Data;

import java.util.List;

/** 语言（含词库组树） */
@Data
public class LanguageVO {

    private Long id;
    private String name;
    private String code;
    private List<GroupVO> groups;
}
