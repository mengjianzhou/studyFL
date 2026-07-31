package com.learnfl.dto.library;

import lombok.Data;

import java.util.List;

/** 词库组（一本书） */
@Data
public class GroupVO {

    private Long id;
    private String name;
    private String description;
    private List<BankVO> banks;
}
