package com.learnfl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnfl.entity.WordBank;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WordBankMapper extends BaseMapper<WordBank> {
}
