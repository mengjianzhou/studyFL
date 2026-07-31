package com.learnfl.dto.practice;

import com.learnfl.dto.progress.ProgressVO;
import lombok.Data;

/** 练习提交响应 */
@Data
public class PracticeSubmitResponse {

    private Long recordId;
    private ProgressVO progress;

    public PracticeSubmitResponse(Long recordId, ProgressVO progress) {
        this.recordId = recordId;
        this.progress = progress;
    }
}
