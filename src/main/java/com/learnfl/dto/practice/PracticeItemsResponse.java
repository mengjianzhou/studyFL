package com.learnfl.dto.practice;

import lombok.Data;

import java.util.List;

/** 练习词表响应 */
@Data
public class PracticeItemsResponse {

    private List<PracticeItemVO> items;
    /** 该词库此模式下总词数（用于进度） */
    private int totalCount;
}
