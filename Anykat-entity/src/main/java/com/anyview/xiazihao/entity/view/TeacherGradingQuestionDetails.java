package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherGradingQuestionDetails {
    // 必要ID
    private Integer exerciseId;      // 练习ID
    private Integer studentId;       // 学生ID
    private Integer questionId;      // 题目ID

    // 批改核心内容
    private String questionContent;  // 题目内容
    private Integer maxScore;        // 题目满分值
    private String studentAnswer;    // 学生答案
    private Integer currentScore;    // 当前得分
    private String referenceAnswer;  // 参考答案

    // 系统记录
    private Integer answerId;        // 答题记录ID(用于更新操作)
}
