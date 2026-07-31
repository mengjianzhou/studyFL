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
import com.learnfl.mapper.PracticeRecordMapper;
import com.learnfl.mapper.SentenceMapper;
import com.learnfl.mapper.UserProgressMapper;
import com.learnfl.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final WordMapper wordMapper;
    private final SentenceMapper sentenceMapper;
    private final PracticeRecordMapper practiceRecordMapper;
    private final UserProgressMapper userProgressMapper;

    /** 取练习词表（浏览分页与练习会话分离，一次取全） */
    public PracticeItemsResponse items(Long bankId, String mode, String order) {
        Long userId = UserContext.userId();
        List<PracticeItemVO> items;
        int totalCount;

        if ("word".equals(mode)) {
            List<Word> words = wordMapper.selectList(
                    new LambdaQueryWrapper<Word>().eq(Word::getWordBankId, bankId));
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
            items = sentences.stream().map(s -> {
                PracticeItemVO vo = new PracticeItemVO();
                vo.setId(s.getId());
                vo.setText(s.getEnglish());
                vo.setPhonetic(null);
                vo.setMeaning(s.getChinese());
                vo.setExtra(s.getJapanese());
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

        ProgressVO progress = updateProgress(userId, req.getBankId(), req.getMode(), req.getTotalWords());
        return new PracticeSubmitResponse(record.getId(), progress);
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
