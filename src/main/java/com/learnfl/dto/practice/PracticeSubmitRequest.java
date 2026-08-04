package com.learnfl.dto.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 练习提交请求 */
@Data
public class PracticeSubmitRequest {

    @NotNull(message = "词库不能为空")
    private Long bankId;

    @NotBlank(message = "模式不能为空")
    private String mode;

    /** asc / shuffle */
    private String orderType = "shuffle";

    @NotNull(message = "总词数不能为空")
    private Integer totalWords;

    private Integer correctFirstWords = 0;
    private Integer errorCount = 0;
    private Integer totalKeystrokes = 0;
    private Long elapsedMs = 0L;
    private Boolean isDictation = false;
    private Integer dictationScore;
    private List<PracticeItemResultRequest> itemResults;
}
