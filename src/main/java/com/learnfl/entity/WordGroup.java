package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 词库组（一本书） */
@Data
@TableName("word_group")
public class WordGroup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long languageId;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
