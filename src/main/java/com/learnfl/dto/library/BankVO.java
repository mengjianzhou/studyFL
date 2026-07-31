package com.learnfl.dto.library;

import com.learnfl.dto.progress.ProgressVO;
import lombok.Data;

/** 词库（一课） */
@Data
public class BankVO {

    private Long id;
    private String name;
    private String description;
    private long wordCount;
    private long sentenceCount;
    private ProgressVO progress;
}
