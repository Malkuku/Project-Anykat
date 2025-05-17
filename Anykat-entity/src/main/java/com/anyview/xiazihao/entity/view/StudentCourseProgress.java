package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseProgress {
    private Integer studentId;          // 学生ID
    private Integer courseId;          // 课程ID
    private Integer semesterId;        // 学期ID
    private Integer exerciseId;        // 练习ID
    private Integer totalExercises;    // 总练习数
    private Integer totalQuestions;    // 总题目数
    private Integer totalScore;        // 总分
    private Integer completedQuestions;// 已完成题目数
    private Integer completedScore;    // 已完成分数
    private Integer exerciseStatus;    // 练习状态(0:未开始,1:进行中,2:已结束)
}