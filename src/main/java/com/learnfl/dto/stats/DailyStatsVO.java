package com.learnfl.dto.stats;

import lombok.Data;

/** 每日统计 */
@Data
public class DailyStatsVO {

    private String date;
    private long count;

    public DailyStatsVO(String date, long count) {
        this.date = date;
        this.count = count;
    }
}
