package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sentence")
public class Sentence {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属词库（一课），而非词库组 */
    private Long wordBankId;
    private String chinese;
    private String english;
    private String japanese;
    private String sentenceType;
    private LocalDateTime createdAt;
}
