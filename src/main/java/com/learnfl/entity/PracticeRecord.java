package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 练习记录（一次会话，统计来源） */
@Data
@TableName("practice_record")
public class PracticeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long wordBankId;
    /** word / sentence */
    private String mode;
    /** asc / shuffle */
    private String orderType;
    private Integer totalWords;
    private Integer correctFirstWords;
    private Integer errorCount;
    private Integer totalKeystrokes;
    private Long elapsedMs;
    private BigDecimal wpm;
    private BigDecimal accuracy;
    private Boolean isDictation;
    private Integer dictationScore;
    private LocalDateTime createdAt;
}
