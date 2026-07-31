package com.learnfl.controller;

import com.learnfl.common.PageResult;
import com.learnfl.common.Result;
import com.learnfl.dto.content.SentenceVO;
import com.learnfl.dto.content.WordVO;
import com.learnfl.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banks/{bankId}")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/words")
    public Result<PageResult<WordVO>> words(@PathVariable Long bankId,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(contentService.words(bankId, page, size));
    }

    @GetMapping("/sentences")
    public Result<PageResult<SentenceVO>> sentences(@PathVariable Long bankId,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size) {
        return Result.ok(contentService.sentences(bankId, page, size));
    }

    /** 统一入口：?mode=word|sentence */
    @GetMapping("/content")
    public Result<Object> content(@PathVariable Long bankId,
                                  @RequestParam String mode,
                                  @RequestParam(defaultValue = "1") long page,
                                  @RequestParam(defaultValue = "10") long size) {
        return Result.ok(contentService.content(bankId, mode, page, size));
    }
}
