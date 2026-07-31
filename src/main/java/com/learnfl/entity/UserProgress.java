package com.learnfl.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户学习进度（每 词库 x 模式 一条） */
@Data
@TableName("user_progress")
public class UserProgress {

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long wordBankId;
    /** word / sentence */
    private String mode;
    private Integer totalCount;
    private Integer completedCount;
    private String status;
    private Integer lastWordIndex;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
