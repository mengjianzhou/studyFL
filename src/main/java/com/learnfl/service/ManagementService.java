package com.learnfl.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learnfl.common.BizException;
import com.learnfl.dto.manage.BankCreateRequest;
import com.learnfl.dto.manage.GroupCreateRequest;
import com.learnfl.dto.manage.SentenceSaveRequest;
import com.learnfl.dto.manage.WordSaveRequest;
import com.learnfl.entity.*;
import com.learnfl.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 词库组 / 词库 / 单词 / 句子的增删改管理 */
@Service
@RequiredArgsConstructor
public class ManagementService {

    private final LanguageMapper languageMapper;
    private final WordGroupMapper wordGroupMapper;
    private final WordBankMapper wordBankMapper;
    private final WordMapper wordMapper;
    private final SentenceMapper sentenceMapper;
    private final UserProgressMapper userProgressMapper;
    private final PracticeRecordMapper practiceRecordMapper;

    // ---------- 词库组 ----------

    public WordGroup createGroup(GroupCreateRequest req) {
        if (languageMapper.selectById(req.getLanguageId()) == null) {
            throw new BizException("语言不存在");
        }
        WordGroup group = new WordGroup();
        group.setLanguageId(req.getLanguageId());
        group.setName(req.getName());
        group.setDescription(req.getDescription());
        group.setCreatedAt(LocalDateTime.now());
        wordGroupMapper.insert(group);
        return group;
    }

    /** 删除词库组：级联删除其下所有词库及数据 */
    @Transactional
    public void deleteGroup(Long groupId) {
        WordGroup group = wordGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BizException("词库组不存在");
        }
        for (WordBank bank : wordBankMapper.selectList(
                new LambdaQueryWrapper<WordBank>().eq(WordBank::getGroupId, groupId))) {
            deleteBankInternal(bank.getId());
        }
        wordGroupMapper.deleteById(groupId);
    }

    // ---------- 词库 ----------

    public WordBank createBank(BankCreateRequest req) {
        if (wordGroupMapper.selectById(req.getGroupId()) == null) {
            throw new BizException("词库组不存在");
        }
        WordBank bank = new WordBank();
        bank.setGroupId(req.getGroupId());
        bank.setName(req.getName());
        bank.setDescription(req.getDescription());
        bank.setCreatedAt(LocalDateTime.now());
        wordBankMapper.insert(bank);
        return bank;
    }

    /** 删除词库：级联删除单词/句子/进度/练习记录 */
    @Transactional
    public void deleteBank(Long bankId) {
        if (wordBankMapper.selectById(bankId) == null) {
            throw new BizException("词库不存在");
        }
        deleteBankInternal(bankId);
    }

    private void deleteBankInternal(Long bankId) {
        wordMapper.delete(new LambdaQueryWrapper<Word>().eq(Word::getWordBankId, bankId));
        sentenceMapper.delete(new LambdaQueryWrapper<Sentence>().eq(Sentence::getWordBankId, bankId));
        userProgressMapper.delete(new LambdaQueryWrapper<UserProgress>().eq(UserProgress::getWordBankId, bankId));
        practiceRecordMapper.delete(new LambdaQueryWrapper<PracticeRecord>().eq(PracticeRecord::getWordBankId, bankId));
        wordBankMapper.deleteById(bankId);
    }

    // ---------- 单词 ----------

    public Word createWord(Long bankId, WordSaveRequest req) {
        ensureBank(bankId);
        Word word = new Word();
        word.setWordBankId(bankId);
        word.setWord(req.getWord());
        word.setPhonetic(req.getPhonetic());
        word.setMeaning(req.getMeaning());
        word.setWordType(req.getWordType());
        word.setCreatedAt(LocalDateTime.now());
        wordMapper.insert(word);
        return word;
    }

    public Word updateWord(Long wordId, WordSaveRequest req) {
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new BizException("单词不存在");
        }
        word.setWord(req.getWord());
        word.setPhonetic(req.getPhonetic());
        word.setMeaning(req.getMeaning());
        word.setWordType(req.getWordType());
        wordMapper.updateById(word);
        return word;
    }

    public void deleteWord(Long wordId) {
        if (wordMapper.selectById(wordId) == null) {
            throw new BizException("单词不存在");
        }
        wordMapper.deleteById(wordId);
    }

    // ---------- 句子 ----------

    public Sentence createSentence(Long bankId, SentenceSaveRequest req) {
        ensureBank(bankId);
        Sentence sentence = new Sentence();
        sentence.setWordBankId(bankId);
        sentence.setEnglish(req.getEnglish());
        sentence.setChinese(req.getChinese());
        sentence.setJapanese(req.getJapanese());
        sentence.setSentenceType(req.getSentenceType());
        sentence.setCreatedAt(LocalDateTime.now());
        sentenceMapper.insert(sentence);
        return sentence;
    }

    public Sentence updateSentence(Long sentenceId, SentenceSaveRequest req) {
        Sentence sentence = sentenceMapper.selectById(sentenceId);
        if (sentence == null) {
            throw new BizException("句子不存在");
        }
        sentence.setEnglish(req.getEnglish());
        sentence.setChinese(req.getChinese());
        sentence.setJapanese(req.getJapanese());
        sentence.setSentenceType(req.getSentenceType());
        sentenceMapper.updateById(sentence);
        return sentence;
    }

    public void deleteSentence(Long sentenceId) {
        if (sentenceMapper.selectById(sentenceId) == null) {
            throw new BizException("句子不存在");
        }
        sentenceMapper.deleteById(sentenceId);
    }

    private void ensureBank(Long bankId) {
        if (wordBankMapper.selectById(bankId) == null) {
            throw new BizException("词库不存在");
        }
    }
}
