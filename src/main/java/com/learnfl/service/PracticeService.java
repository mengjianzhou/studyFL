package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnfl.common.BizException;
import com.learnfl.common.UserContext;
import com.learnfl.dto.practice.PracticeItemVO;
import com.learnfl.dto.practice.PracticeItemsResponse;
import com.learnfl.dto.practice.PracticeSubmitRequest;
import com.learnfl.dto.practice.PracticeSubmitResponse;
import com.learnfl.dto.progress.ProgressVO;
import com.learnfl.entity.*;
import com.learnfl.mapper.LanguageMapper;
import com.learnfl.mapper.PracticeRecordMapper;
import com.learnfl.mapper.SentenceMapper;
import com.learnfl.mapper.UserProgressMapper;
import com.learnfl.mapper.WordBankMapper;
import com.learnfl.mapper.WordGroupMapper;
import com.learnfl.mapper.WordMapper;
import com.learnfl.mapper.UserWordStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final WordMapper wordMapper;
    private final SentenceMapper sentenceMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final UserProgressMapper userProgressMapper;
    private final WordBankMapper wordBankMapper;
    private final WordGroupMapper wordGroupMapper;
    private final LanguageMapper languageMapper;
    private final UserWordStatusMapper userWordStatusMapper;
    private final MemoryRuleService memoryRuleService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 取练习词表（浏览分页与练习会话分离，一次取全） */
    public PracticeItemsResponse items(Long bankId, String mode, String order) {
        Long userId = UserContext.userId();
        List<PracticeItemVO> items;
        int totalCount;

        if ("word".equals(mode)) {
            List<Word> words = wordMapper.selectList(
                    new LambdaQueryWrapper<Word>().eq(Word::getWordBankId, bankId));
            MemoryRule activeRule = memoryRuleService.activeRule();
            if (activeRule != null && !words.isEmpty()) {
                List<Long> wordIds = words.stream().map(Word::getId).toList();
                Map<Long, UserWordStatus> statusByWordId = userWordStatusMapper.selectList(
                                new LambdaQueryWrapper<UserWordStatus>()
                                        .eq(UserWordStatus::getUserId, userId)
                                        .in(UserWordStatus::getWordId, wordIds))
                        .stream().collect(Collectors.toMap(UserWordStatus::getWordId, status -> status));
                LocalDate today = LocalDate.now();
                words = words.stream().filter(word -> {
                    UserWordStatus status = statusByWordId.get(word.getId());
                    return status == null || status.getNextReviewDate() == null || !status.getNextReviewDate().isAfter(today);
                }).collect(Collectors.toList());
            }
            totalCount = words.size();
            items = words.stream().map(w -> {
                PracticeItemVO vo = new PracticeItemVO();
                vo.setId(w.getId());
                vo.setText(w.getWord());
                vo.setPhonetic(w.getPhonetic());
                vo.setMeaning(w.getMeaning());
                vo.setExtra(w.getWordType());
                return vo;
            }).collect(Collectors.toList());
        } else if ("sentence".equals(mode)) {
            List<Sentence> sentences = sentenceMapper.selectList(
                    new LambdaQueryWrapper<Sentence>().eq(Sentence::getWordBankId, bankId));
            totalCount = sentences.size();
            // 按词库语言决定练习内容：日语词库打日文，英语词库打英文（缺失时回退另一语言）
            boolean isJa = isJapaneseBank(bankId);
            items = sentences.stream().map(s -> {
                PracticeItemVO vo = new PracticeItemVO();
                vo.setId(s.getId());
                String primary = isJa ? s.getJapanese() : s.getEnglish();
                String fallback = isJa ? s.getEnglish() : s.getJapanese();
                vo.setText(primary != null ? primary : fallback);
                // 保留句子原始字段，编辑页需要区分英文翻译和切分单元中的罗马音。
                vo.setEnglish(s.getEnglish());
                vo.setChinese(s.getChinese());
                vo.setJapanese(s.getJapanese());
                // 兼容现有练习页：日语句子的 phonetic 仍提供原 english 字段。
                vo.setPhonetic(isJa ? s.getEnglish() : null);
                vo.setMeaning(s.getChinese());
                vo.setExtra(s.getJapanese());
                // 解析 segmentsJson → segments 列表
                if (s.getSegmentsJson() != null && !s.getSegmentsJson().isBlank()) {
                    try {
                        List<Map<String, String>> segments = objectMapper.readValue(
                                s.getSegmentsJson(), new TypeReference<List<Map<String, String>>>() {});
                        vo.setSegments(segments);
                    } catch (Exception ignored) {
                        // JSON 解析失败时忽略
                    }
                }
                return vo;
            }).collect(Collectors.toList());
        } else {
            throw new BizException("mode 必须为 word 或 sentence");
        }

        if ("shuffle".equals(order) && items.size() > 1) {
            Collections.shuffle(items);
        }
        PracticeItemsResponse resp = new PracticeItemsResponse();
        resp.setItems(items);
        resp.setTotalCount(totalCount);
        return resp;
    }

    /** 提交练习记录：落库 + 更新进度 */
    @Transactional
    public PracticeSubmitResponse submit(PracticeSubmitRequest req) {
        Long userId = UserContext.userId();
        if (!"word".equals(req.getMode()) && !"sentence".equals(req.getMode())) {
            throw new BizException("mode 必须为 word 或 sentence");
        }
        if (req.getTotalKeystrokes() <= 0) {
            throw new BizException("无效的练习数据");
        }

        // 计算 wpm / accuracy（与前端口径一致，后端兜底校验）
        int totalKeystrokes = req.getTotalKeystrokes();
        int errorCount = Math.min(req.getErrorCount(), totalKeystrokes);
        BigDecimal accuracy = BigDecimal.valueOf(totalKeystrokes - errorCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalKeystrokes), 2, RoundingMode.HALF_UP);
        BigDecimal wpm = req.getElapsedMs() > 0
                ? BigDecimal.valueOf(req.getTotalWords())
                    .multiply(BigDecimal.valueOf(60000))
                    .divide(BigDecimal.valueOf(req.getElapsedMs()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setWordBankId(req.getBankId());
        record.setMode(req.getMode());
        record.setOrderType(req.getOrderType());
        record.setTotalWords(req.getTotalWords());
        record.setCorrectFirstWords(req.getCorrectFirstWords());
        record.setErrorCount(errorCount);
        record.setTotalKeystrokes(totalKeystrokes);
        record.setElapsedMs(req.getElapsedMs());
        record.setWpm(wpm);
        record.setAccuracy(accuracy);
        record.setIsDictation(req.getIsDictation());
        record.setDictationScore(req.getDictationScore());
        record.setCreatedAt(LocalDateTime.now());
        practiceRecordMapper.insert(record);

        if ("word".equals(req.getMode())) {
            updateWordMemory(userId, req);
        }

        ProgressVO progress = updateProgress(userId, req.getBankId(), req.getMode(), req.getTotalWords());
        return new PracticeSubmitResponse(record.getId(), progress);
    }

    private void updateWordMemory(Long userId, PracticeSubmitRequest req) {
        MemoryRule rule = memoryRuleService.activeRule();
        if (rule == null || req.getItemResults() == null || req.getItemResults().isEmpty()) return;

        Set<Long> submittedIds = req.getItemResults().stream()
                .map(result -> result.getItemId())
                .collect(Collectors.toSet());
        Set<Long> allowedIds = new HashSet<>(wordMapper.selectList(
                        new LambdaQueryWrapper<Word>()
                                .eq(Word::getWordBankId, req.getBankId())
                                .in(Word::getId, submittedIds))
                .stream().map(Word::getId).toList());

        Map<Long, Boolean> resultByWordId = new HashMap<>();
        req.getItemResults().stream()
                .filter(result -> allowedIds.contains(result.getItemId()))
                .forEach(result -> resultByWordId.merge(result.getItemId(), result.getSuccess(), (oldValue, newValue) -> oldValue && newValue));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        for (Map.Entry<Long, Boolean> result : resultByWordId.entrySet()) {
            UserWordStatus status = userWordStatusMapper.selectOne(
                    new LambdaQueryWrapper<UserWordStatus>()
                            .eq(UserWordStatus::getUserId, userId)
                            .eq(UserWordStatus::getWordId, result.getKey()));
            if (status == null) {
                status = new UserWordStatus();
                status.setUserId(userId);
                status.setWordId(result.getKey());
                status.setConsecutiveSuccess(0);
                status.setTotalSuccess(0);
                status.setTotalFailure(0);
                status.setCreatedAt(now);
            }

            int intervalDays;
            if (result.getValue()) {
                int consecutive = status.getConsecutiveSuccess() + 1;
                status.setConsecutiveSuccess(consecutive);
                status.setTotalSuccess(status.getTotalSuccess() + 1);
                status.setLastResult(UserWordStatus.RESULT_SUCCESS);
                intervalDays = successIntervalDays(rule, consecutive);
            } else {
                status.setConsecutiveSuccess(0);
                status.setTotalFailure(status.getTotalFailure() + 1);
                status.setLastResult(UserWordStatus.RESULT_FAILURE);
                intervalDays = rule.getFailureIntervalDays();
            }
            status.setLastReviewedAt(now);
            status.setNextReviewDate(today.plusDays(intervalDays));
            status.setUpdatedAt(now);
            if (status.getId() == null) userWordStatusMapper.insert(status);
            else userWordStatusMapper.updateById(status);
        }
    }

    private int successIntervalDays(MemoryRule rule, int consecutiveSuccess) {
        if (consecutiveSuccess >= rule.getLevel3SuccessCount()) return rule.getLevel3IntervalDays();
        if (consecutiveSuccess >= rule.getLevel2SuccessCount()) return rule.getLevel2IntervalDays();
        if (consecutiveSuccess >= rule.getLevel1SuccessCount()) return rule.getLevel1IntervalDays();
        return rule.getFirstSuccessDays();
    }

    /** 判断词库是否属于日语（词库 → 词库组 → 语言） */
    private boolean isJapaneseBank(Long bankId) {
        WordBank bank = wordBankMapper.selectById(bankId);
        if (bank == null) return false;
        WordGroup group = wordGroupMapper.selectById(bank.getGroupId());
        if (group == null) return false;
        Language lang = languageMapper.selectById(group.getLanguageId());
        return lang != null && "ja".equals(lang.getCode());
    }

    /** 查询某词库某模式进度 */
    public ProgressVO progress(Long bankId, String mode) {
        UserProgress p = findProgress(UserContext.userId(), bankId, mode);
        return p == null ? null : toProgressVO(p);
    }

    /** 更新进度：完成全部词 → COMPLETED */
    private ProgressVO updateProgress(Long userId, Long bankId, String mode, int totalWords) {
        UserProgress p = findProgress(userId, bankId, mode);
        if (p == null) {
            p = new UserProgress();
            p.setUserId(userId);
            p.setWordBankId(bankId);
            p.setMode(mode);
            p.setTotalCount(totalWords);
            p.setCompletedCount(totalWords);
            p.setStatus(UserProgress.STATUS_COMPLETED);
            p.setCompletedAt(LocalDateTime.now());
            userProgressMapper.insert(p);
        } else {
            p.setCompletedCount(totalWords);
            p.setStatus(UserProgress.STATUS_COMPLETED);
            if (p.getCompletedAt() == null) {
                p.setCompletedAt(LocalDateTime.now());
            }
            userProgressMapper.updateById(p);
        }
        return toProgressVO(p);
    }

    private UserProgress findProgress(Long userId, Long bankId, String mode) {
        return userProgressMapper.selectOne(new LambdaQueryWrapper<UserProgress>()
                .eq(UserProgress::getUserId, userId)
                .eq(UserProgress::getWordBankId, bankId)
                .eq(UserProgress::getMode, mode));
    }

    private ProgressVO toProgressVO(UserProgress p) {
        ProgressVO vo = new ProgressVO();
        vo.setStatus(p.getStatus());
        vo.setTotalCount(p.getTotalCount());
        vo.setCompletedCount(p.getCompletedCount());
        vo.setCompletedAt(p.getCompletedAt());
        return vo;
    }
}
