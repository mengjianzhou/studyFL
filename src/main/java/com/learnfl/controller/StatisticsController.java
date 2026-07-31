package com.learnfl.controller;

import com.learnfl.common.Result;
import com.learnfl.dto.stats.BankStatsVO;
import com.learnfl.dto.stats.StatisticsVO;
import com.learnfl.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /** 用户整体统计 */
    @GetMapping("/me")
    public Result<StatisticsVO> me(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(statisticsService.me(days));
    }

    /** 各词库统计 */
    @GetMapping("/banks")
    public Result<List<BankStatsVO>> banks() {
        return Result.ok(statisticsService.banks());
    }
}
