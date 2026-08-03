package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learnfl.common.BizException;
import com.learnfl.common.PageResult;
import com.learnfl.dto.content.SentenceVO;
import com.learnfl.dto.content.WordVO;
import com.learnfl.entity.Sentence;
import com.learnfl.entity.Word;
import com.learnfl.mapper.SentenceMapper;
import com.learnfl.mapper.WordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final WordMapper wordMapper;
    private final SentenceMapper sentenceMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 单词分页 */
    public PageResult<WordVO> words(Long bankId, long page, long size) {
        Page<Word> p = wordMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Word>().eq(Word::getWordBankId, bankId).orderByAsc(Word::getId));
        return PageResult.of(p.convert(w -> {
            WordVO vo = new WordVO();
            vo.setId(w.getId());
            vo.setWord(w.getWord());
            vo.setPhonetic(w.getPhonetic());
            vo.setMeaning(w.getMeaning());
            vo.setWordType(w.getWordType());
            return vo;
        }));
    }

    /** 句子分页 */
    public PageResult<SentenceVO> sentences(Long bankId, long page, long size) {
        Page<Sentence> p = sentenceMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Sentence>().eq(Sentence::getWordBankId, bankId).orderByAsc(Sentence::getId));
        return PageResult.of(p.convert(s -> {
            SentenceVO vo = new SentenceVO();
            vo.setId(s.getId());
            vo.setEnglish(s.getEnglish());
            vo.setChinese(s.getChinese());
            vo.setJapanese(s.getJapanese());
            vo.setSentenceType(s.getSentenceType());
            // 解析 segmentsJson → segments 列表
            if (s.getSegmentsJson() != null && !s.getSegmentsJson().isBlank()) {
                try {
                    List<Map<String, String>> segments = objectMapper.readValue(
                            s.getSegmentsJson(), new TypeReference<List<Map<String, String>>>() {});
                    vo.setSegments(segments);
                } catch (Exception e) {
                    // JSON 解析失败时忽略，segments 保持 null
                }
            }
            return vo;
        }));
    }

    /** 统一入口：mode=word|sentence */
    public Object content(Long bankId, String mode, long page, long size) {
        if ("word".equals(mode)) {
            return words(bankId, page, size);
        } else if ("sentence".equals(mode)) {
            return sentences(bankId, page, size);
        }
        throw new BizException("mode 必须为 word 或 sentence");
    }
}
