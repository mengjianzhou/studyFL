package com.learnfl.controller;

import com.learnfl.common.Result;
import com.learnfl.dto.manage.BankCreateRequest;
import com.learnfl.dto.manage.GroupCreateRequest;
import com.learnfl.dto.manage.SentenceSaveRequest;
import com.learnfl.dto.manage.WordSaveRequest;
import com.learnfl.entity.Sentence;
import com.learnfl.entity.Word;
import com.learnfl.entity.WordBank;
import com.learnfl.entity.WordGroup;
import com.learnfl.service.ManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 词库组 / 词库 / 单词 / 句子管理（所有登录用户可操作） */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService managementService;

    // ---------- 词库组 ----------

    @PostMapping("/groups")
    public Result<WordGroup> createGroup(@Valid @RequestBody GroupCreateRequest req) {
        return Result.ok(managementService.createGroup(req));
    }

    @DeleteMapping("/groups/{id}")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        managementService.deleteGroup(id);
        return Result.ok();
    }

    // ---------- 词库 ----------

    @PostMapping("/banks")
    public Result<WordBank> createBank(@Valid @RequestBody BankCreateRequest req) {
        return Result.ok(managementService.createBank(req));
    }

    @DeleteMapping("/banks/{id}")
    public Result<Void> deleteBank(@PathVariable Long id) {
        managementService.deleteBank(id);
        return Result.ok();
    }

    // ---------- 单词 ----------

    @PostMapping("/banks/{bankId}/words")
    public Result<Word> createWord(@PathVariable Long bankId, @Valid @RequestBody WordSaveRequest req) {
        return Result.ok(managementService.createWord(bankId, req));
    }

    @PutMapping("/words/{id}")
    public Result<Word> updateWord(@PathVariable Long id, @Valid @RequestBody WordSaveRequest req) {
        return Result.ok(managementService.updateWord(id, req));
    }

    @DeleteMapping("/words/{id}")
    public Result<Void> deleteWord(@PathVariable Long id) {
        managementService.deleteWord(id);
        return Result.ok();
    }

    // ---------- 句子 ----------

    @PostMapping("/banks/{bankId}/sentences")
    public Result<Sentence> createSentence(@PathVariable Long bankId, @Valid @RequestBody SentenceSaveRequest req) {
        return Result.ok(managementService.createSentence(bankId, req));
    }

    @PutMapping("/sentences/{id}")
    public Result<Sentence> updateSentence(@PathVariable Long id, @Valid @RequestBody SentenceSaveRequest req) {
        return Result.ok(managementService.updateSentence(id, req));
    }

    @DeleteMapping("/sentences/{id}")
    public Result<Void> deleteSentence(@PathVariable Long id) {
        managementService.deleteSentence(id);
        return Result.ok();
    }
}
