package com.anyview.xiazihao.dao.impl;


import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.QuestionDao;
import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class QuestionDaoImpl implements QuestionDao {

    @Override
    public Integer selectQuestionCount(QuestionQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT COUNT(*)
            FROM base_question
            WHERE 1=1\s
                AND (#{type} IS NULL OR type = #{type})
                AND (#{description} IS NULL OR description LIKE CONCAT('%', #{description}, '%'))
                AND (#{content} IS NULL OR content LIKE CONCAT('%', #{content}, '%'))
                AND (#{minDifficulty} IS NULL OR difficulty >= #{minDifficulty})
                AND (#{maxDifficulty} IS NULL OR difficulty <= #{maxDifficulty})
                AND (#{minScore} IS NULL OR score >= #{minScore})
                AND (#{maxScore} IS NULL OR score <= #{maxScore})
                AND (#{creatorId} IS NULL OR creator_id = #{creatorId})
            """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<BaseQuestion> selectQuestionByPage(QuestionQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM base_question
            WHERE 1=1\s
                AND (#{type} IS NULL OR type = #{type})
                AND (#{description} IS NULL OR description LIKE CONCAT('%', #{description}, '%'))
                AND (#{content} IS NULL OR content LIKE CONCAT('%', #{content}, '%'))
                AND (#{minDifficulty} IS NULL OR difficulty >= #{minDifficulty})
                AND (#{maxDifficulty} IS NULL OR difficulty <= #{maxDifficulty})
                AND (#{minScore} IS NULL OR score >= #{minScore})
                AND (#{maxScore} IS NULL OR score <= #{maxScore})
                AND (#{creatorId} IS NULL OR creator_id = #{creatorId})
            ORDER BY updated_at DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """;
        return JdbcUtils.executeQuery(
                sql,
                BaseQuestion.class,
                param
        );
    }
}
