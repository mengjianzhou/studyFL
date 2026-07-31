package com.learnfl.dto.progress;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户在某词库某模式下的进度 */
@Data
public class ProgressVO {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private String status;
    private Integer totalCount;
    private Integer completedCount;
    private LocalDateTime completedAt;
}
