package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherGradingQuestions {
    private Integer exerciseId;  // 练习ID
    private Integer questionId;       // 题目ID
    private Integer studentId;       // 学生ID
    private String questionName;     // 题目名称/内容
    private Integer correctStatus;    // 批改状态(0:保存未提交,1:未批改,2:已批改)
    private Double gradingScore; // 得分
}
