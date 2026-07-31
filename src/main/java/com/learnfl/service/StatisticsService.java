package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnfl.common.UserContext;
import com.learnfl.dto.stats.BankStatsVO;
import com.learnfl.dto.stats.DailyStatsVO;
import com.learnfl.dto.stats.StatisticsVO;
import com.learnfl.entity.UserProgress;
import com.learnfl.entity.WordBank;
import com.learnfl.entity.WordGroup;
import com.learnfl.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PracticeRecordMapper practiceRecordMapper;
    private final WordBankMapper wordBankMapper;
    private final WordGroupMapper wordGroupMapper;
    private final LanguageMapper languageMapper;
    private final UserProgressMapper userProgressMapper;

    /** 用户整体统计（近 N 天） */
    public StatisticsVO me(int days) {
        Long userId = UserContext.userId();
        StatisticsVO vo = new StatisticsVO();
        vo.setTotalPractices(practiceRecordMapper.countPractices(userId));
        vo.setTotalKeystrokes(practiceRecordMapper.sumKeystrokes(userId));
        vo.setAvgAccuracy(practiceRecordMapper.avgAccuracy(userId));
        vo.setAvgWpm(practiceRecordMapper.avgWpm(userId));

        LocalDate since = LocalDate.now().minusDays(days);
        vo.setDaily(practiceRecordMapper.countByDay(userId, since).stream()
                .map(r -> new DailyStatsVO(String.valueOf(r.get("day")), ((Number) r.get("cnt")).longValue()))
                .collect(Collectors.toList()));
        return vo;
    }

    /** 各词库练习统计 */
    public List<BankStatsVO> banks() {
        Long userId = UserContext.userId();
        Map<Long, Map<String, Object>> byBank = practiceRecordMapper.statsByBank(userId).stream()
                .collect(Collectors.toMap(r -> ((Number) r.get("bankId")).longValue(), r -> r));

        // 所有词库（含从未练习的，进度展示用）
        return wordBankMapper.selectList(new LambdaQueryWrapper<WordBank>().orderByAsc(WordBank::getId)).stream()
                .map(bank -> {
                    BankStatsVO vo = new BankStatsVO();
                    vo.setBankId(bank.getId());
                    vo.setBankName(bank.getName());
                    WordGroup group = wordGroupMapper.selectById(bank.getGroupId());
                    if (group != null) {
                        vo.setGroupName(group.getName());
                        vo.setLanguageCode(languageMapper.selectById(group.getLanguageId()).getCode());
                    }
                    Map<String, Object> stats = byBank.get(bank.getId());
                    vo.setPractices(stats == null ? 0 : ((Number) stats.get("practices")).longValue());
                    vo.setAvgAccuracy(stats == null ? BigDecimal.ZERO : (BigDecimal) stats.get("avgAccuracy"));
                    vo.setAvgWpm(stats == null ? BigDecimal.ZERO : (BigDecimal) stats.get("avgWpm"));

                    UserProgress p = userProgressMapper.selectOne(new LambdaQueryWrapper<UserProgress>()
                            .eq(UserProgress::getUserId, userId)
                            .eq(UserProgress::getWordBankId, bank.getId()));
                    vo.setProgressStatus(p == null ? null : p.getStatus());
                    return vo;
                }).collect(Collectors.toList());
    }
}
