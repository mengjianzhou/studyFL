package com.learnfl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learnfl.entity.PracticeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface PracticeRecordMapper extends BaseMapper<PracticeRecord> {

    /** 近 N 天每日练习次数 */
    @Select("SELECT DATE(created_at) AS day, COUNT(*) AS cnt " +
            "FROM practice_record " +
            "WHERE user_id = #{userId} AND created_at >= #{since} " +
            "GROUP BY DATE(created_at) ORDER BY day")
    List<Map<String, Object>> countByDay(Long userId, LocalDate since);

    /** 累计练习次数 */
    @Select("SELECT COUNT(*) FROM practice_record WHERE user_id = #{userId}")
    long countPractices(Long userId);

    /** 累计打字字符数 */
    @Select("SELECT IFNULL(SUM(total_keystrokes), 0) FROM practice_record WHERE user_id = #{userId}")
    long sumKeystrokes(Long userId);

    /** 平均正确率 % */
    @Select("SELECT IFNULL(AVG(accuracy), 0) FROM practice_record WHERE user_id = #{userId}")
    BigDecimal avgAccuracy(Long userId);

    /** 平均速度 WPM */
    @Select("SELECT IFNULL(AVG(wpm), 0) FROM practice_record WHERE user_id = #{userId}")
    BigDecimal avgWpm(Long userId);

    /** 各词库练习统计 */
    @Select("SELECT word_bank_id AS bankId, COUNT(*) AS practices, " +
            "IFNULL(AVG(accuracy), 0) AS avgAccuracy, IFNULL(AVG(wpm), 0) AS avgWpm " +
            "FROM practice_record WHERE user_id = #{userId} " +
            "GROUP BY word_bank_id")
    List<Map<String, Object>> statsByBank(Long userId);
}
