package com.learnfl.controller;

import com.learnfl.common.Result;
import com.learnfl.dto.practice.PracticeItemsResponse;
import com.learnfl.dto.practice.PracticeSubmitRequest;
import com.learnfl.dto.practice.PracticeSubmitResponse;
import com.learnfl.dto.progress.ProgressVO;
import com.learnfl.service.PracticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    /** 练习词表（order=asc|shuffle，默认 shuffle） */
    @GetMapping("/practices/words")
    public Result<PracticeItemsResponse> items(@RequestParam Long bankId,
                                               @RequestParam String mode,
                                               @RequestParam(defaultValue = "shuffle") String order) {
        return Result.ok(practiceService.items(bankId, mode, order));
    }

    /** 提交练习记录 */
    @PostMapping("/practices/records")
    public Result<PracticeSubmitResponse> submit(@Valid @RequestBody PracticeSubmitRequest req) {
        return Result.ok(practiceService.submit(req));
    }

    /** 查询某词库某模式进度 */
    @GetMapping("/progress")
    public Result<ProgressVO> progress(@RequestParam Long bankId, @RequestParam String mode) {
        return Result.ok(practiceService.progress(bankId, mode));
    }
}
