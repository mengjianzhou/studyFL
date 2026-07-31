package com.learnfl.dto.stats;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 用户整体统计 */
@Data
public class StatisticsVO {

    private long totalPractices;
    private long totalKeystrokes;
    private BigDecimal avgAccuracy;
    private BigDecimal avgWpm;
    /** 近 N 天每日练习次数 */
    private List<DailyStatsVO> daily;
}
