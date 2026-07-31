package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 词库（一课） */
@Data
@TableName("word_bank")
public class WordBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;
    private String name;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
