package com.learnfl.controller;

import com.learnfl.common.Result;
import com.learnfl.dto.library.BankVO;
import com.learnfl.dto.library.LanguageVO;
import com.learnfl.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    /** 完整词库树：语言 → 词库组 → 词库 */
    @GetMapping("/library/tree")
    public Result<List<LanguageVO>> tree() {
        return Result.ok(libraryService.tree());
    }

    /** 词库详情（含双模式计数与进度） */
    @GetMapping("/banks/{id}")
    public Result<BankVO> bankDetail(@PathVariable Long id) {
        return Result.ok(libraryService.bankDetail(id));
    }
}
