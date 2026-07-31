package com.learnfl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnfl.entity.Sentence;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SentenceMapper extends BaseMapper<Sentence> {
}
