package com.learnfl.dto.stats;

import lombok.Data;

import java.math.BigDecimal;

/** 各词库统计 */
@Data
public class BankStatsVO {

    private Long bankId;
    private String bankName;
    private String groupName;
    private String languageCode;
    private long practices;
    private BigDecimal avgAccuracy;
    private BigDecimal avgWpm;
    /** 词库进度（取任一模式） */
    private String progressStatus;
}
