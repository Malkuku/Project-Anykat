package com.anyview.xiazihao.dao.impl;


import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.QuestionDao;
import com.anyview.xiazihao.entity.param.QuestionQueryParam;
import com.anyview.xiazihao.entity.pojo.question.BaseQuestion;
import com.anyview.xiazihao.entity.pojo.question.ChoiceQuestion;
import com.anyview.xiazihao.entity.pojo.question.SubjectiveQuestion;
import com.anyview.xiazihao.utils.JdbcUtils;
import com.anyview.xiazihao.utils.JsonUtils;
import com.google.gson.annotations.JsonAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
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

    @Override
    public void deleteQuestionsByIds(List<Integer> ids) throws SQLException, FileNotFoundException {
        String sql = "DELETE FROM base_question WHERE id IN (" +
                String.join(",", ids.stream().map(id -> "?").toArray(String[]::new)) +
                ")";
        JdbcUtils.executeUpdate(sql,ids.toArray());
    }

    @Override
    public void addQuestion(BaseQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            INSERT INTO base_question
                (type, description, content, difficulty, score, creator_id)
            VALUES
                (#{type}, #{description}, #{content}, #{difficulty}, #{score}, #{creatorId})""";
        JdbcUtils.executeUpdate(sql, question);
    }

    @Override
    public void updateQuestion(BaseQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE base_question
            SET
                type = COALESCE(#{type}, type),
                description = COALESCE(#{description}, description),
                content = COALESCE(#{content}, content),
                difficulty = COALESCE(#{difficulty}, difficulty),
                score = COALESCE(#{score}, score),
                updated_at = NOW()
            WHERE id = #{id}""";
        JdbcUtils.executeUpdate(sql, question);
    }

    @Override
    public BaseQuestion selectQuestionById(Integer id) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM base_question
            WHERE id = ?""";
        List<BaseQuestion> questions = JdbcUtils.executeQuery(sql, BaseQuestion.class, id);
        return questions.isEmpty() ? null : questions.get(0);
    }

    @Override
    public ChoiceQuestion selectChoiceQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM choice_question
            WHERE question_id = ?""";
        List<ChoiceQuestion> questions = JdbcUtils.executeQuery(sql, ChoiceQuestion.class, questionId);
        return questions.isEmpty() ? null : questions.get(0);
    }

    @Override
    public void addChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            INSERT INTO choice_question
                (question_id, is_multi, options, correct_answer, analysis)
            VALUES
                (#{questionId}, #{isMulti}, #{options}, #{correctAnswer}, #{analysis})""";
        JdbcUtils.executeUpdate(sql, question,"options", JsonUtils.toJson(question.getOptions()));
    }

    @Override
    public void updateChoiceQuestion(ChoiceQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE choice_question
            SET
                is_multi = COALESCE(#{isMulti}, is_multi),
                options = COALESCE(#{options}, options),
                correct_answer = COALESCE(#{correctAnswer}, correct_answer),
                analysis = COALESCE(#{analysis}, analysis)
            WHERE id = #{id}""";
        JdbcUtils.executeUpdate(sql,
                question,
                "options", question.getOptions() == null ? null : JsonUtils.toJson(question.getOptions()));
    }

    @Override
    public SubjectiveQuestion selectSubjectiveQuestionByQuestionId(Integer questionId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM subjective_question
            WHERE question_id = ?""";
        List<SubjectiveQuestion> questions = JdbcUtils.executeQuery(sql, SubjectiveQuestion.class, questionId);
        return questions.isEmpty() ? null : questions.get(0);
    }

    @Override
    public void addSubjectiveQuestion(SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            INSERT INTO subjective_question
                (question_id, reference_answer, word_limit)
            VALUES
                (#{questionId}, #{referenceAnswer}, #{wordLimit})""";
        JdbcUtils.executeUpdate(sql, question);
    }

    @Override
    public void updateSubjectiveQuestion(SubjectiveQuestion question) throws SQLException, FileNotFoundException {
        String sql = """
            UPDATE subjective_question
            SET
                reference_answer = COALESCE(#{referenceAnswer}, reference_answer),
                word_limit = COALESCE(#{wordLimit}, word_limit)
            WHERE id = #{id}""";
        JdbcUtils.executeUpdate(sql, question);
    }
}
