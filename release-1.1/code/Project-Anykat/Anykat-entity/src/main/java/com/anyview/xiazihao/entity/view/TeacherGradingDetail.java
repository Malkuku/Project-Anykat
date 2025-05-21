package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherGradingDetail {
    private Integer exerciseId;          // 练习ID
    private String exerciseName;        // 练习名称

    private Integer teacherId;  // 教师ID
    private String teacherName; //  教师姓名

    private String classNames;           // 班级名称

    private Integer studentId;         // 学生ID
    private String studentName;        // 学生姓名

    // 题目完成情况统计
    private Integer answeredQuestions;  // 已回答题目数
    private Integer totalQuestions;     // 总题目数

    // 批改状态统计
    private Integer savedUnsubmittedCount;    // 已保存未提交数
    private Integer submittedUncorrectedCount; // 已提交未批改数
    private Integer correctedCount;           // 已批改数

    // 分数统计
    private Integer currentScore;      // 当前得分
    private Integer maxScore;          // 满分

    // 时间信息
    private LocalDateTime lastSubmitTime;  // 最后提交时间
    private LocalDateTime lastCorrectTime; // 最后批改时间
}