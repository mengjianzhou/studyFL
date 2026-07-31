package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnfl.common.UserContext;
import com.learnfl.dto.library.BankVO;
import com.learnfl.dto.library.GroupVO;
import com.learnfl.dto.library.LanguageVO;
import com.learnfl.dto.progress.ProgressVO;
import com.learnfl.entity.*;
import com.learnfl.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LanguageMapper languageMapper;
    private final WordGroupMapper wordGroupMapper;
    private final WordBankMapper wordBankMapper;
    private final WordMapper wordMapper;
    private final SentenceMapper sentenceMapper;
    private final UserProgressMapper userProgressMapper;

    /** 完整词库树：语言 → 词库组 → 词库（含计数与进度），一次取全 */
    public List<LanguageVO> tree() {
        Long userId = UserContext.userId();
        List<Language> languages = languageMapper.selectList(
                new LambdaQueryWrapper<Language>().orderByAsc(Language::getSortOrder));
        return languages.stream().map(lang -> {
            LanguageVO langVO = new LanguageVO();
            langVO.setId(lang.getId());
            langVO.setName(lang.getName());
            langVO.setCode(lang.getCode());

            List<WordGroup> groups = wordGroupMapper.selectList(
                    new LambdaQueryWrapper<WordGroup>()
                            .eq(WordGroup::getLanguageId, lang.getId())
                            .orderByAsc(WordGroup::getSortOrder));
            langVO.setGroups(groups.stream().map(g -> {
                GroupVO groupVO = new GroupVO();
                groupVO.setId(g.getId());
                groupVO.setName(g.getName());
                groupVO.setDescription(g.getDescription());

                List<WordBank> banks = wordBankMapper.selectList(
                        new LambdaQueryWrapper<WordBank>()
                                .eq(WordBank::getGroupId, g.getId())
                                .orderByAsc(WordBank::getSortOrder));
                groupVO.setBanks(banks.stream().map(b -> toBankVO(b, userId)).collect(Collectors.toList()));
                return groupVO;
            }).collect(Collectors.toList()));
            return langVO;
        }).collect(Collectors.toList());
    }

    /** 词库详情 */
    public BankVO bankDetail(Long bankId) {
        WordBank bank = wordBankMapper.selectById(bankId);
        if (bank == null) {
            throw new com.learnfl.common.BizException("词库不存在");
        }
        return toBankVO(bank, UserContext.userId());
    }

    private BankVO toBankVO(WordBank bank, Long userId) {
        BankVO vo = new BankVO();
        vo.setId(bank.getId());
        vo.setName(bank.getName());
        vo.setDescription(bank.getDescription());
        vo.setWordCount(wordMapper.selectCount(new LambdaQueryWrapper<Word>().eq(Word::getWordBankId, bank.getId())));
        vo.setSentenceCount(sentenceMapper.selectCount(new LambdaQueryWrapper<Sentence>().eq(Sentence::getWordBankId, bank.getId())));

        // 进度：word 模式优先，其次 sentence
        List<UserProgress> progresses = userProgressMapper.selectList(
                new LambdaQueryWrapper<UserProgress>().eq(UserProgress::getUserId, userId));
        Map<String, UserProgress> byMode = progresses.stream()
                .filter(p -> p.getWordBankId().equals(bank.getId()))
                .collect(Collectors.toMap(UserProgress::getMode, p -> p));
        ProgressVO progress = toProgressVO(byMode.get("word"));
        if (progress == null) {
            progress = toProgressVO(byMode.get("sentence"));
        }
        vo.setProgress(progress);
        return vo;
    }

    private ProgressVO toProgressVO(UserProgress p) {
        if (p == null) {
            return null;
        }
        ProgressVO vo = new ProgressVO();
        vo.setStatus(p.getStatus());
        vo.setTotalCount(p.getTotalCount());
        vo.setCompletedCount(p.getCompletedCount());
        vo.setCompletedAt(p.getCompletedAt());
        return vo;
    }
}
