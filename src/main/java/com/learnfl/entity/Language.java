package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("language")
public class Language {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String code;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
