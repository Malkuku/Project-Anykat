package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentExerciseView {
    private Integer exerciseId;             // 练习ID
    private String exerciseName;           // 练习名称
    private Integer courseId;              // 课程ID
    private String courseName;             // 课程名称
    private LocalDateTime startTime;       // 练习开始时间
    private LocalDateTime endTime;         // 练习结束时间
    private Integer status;                // 练习状态
    private Integer creatorId;             // 创建者ID
    private String creatorName;            // 创建者姓名
    private Integer semesterId;            // 学期ID
    private String semesterName;           // 学期名称
    private Integer studentId;             // 学生ID
    private Integer totalQuestionCount;    // 总题目数
    private Double totalExerciseScore;     // 练习总分
    private Integer completedQuestionCount; // 已完成题目数
    private Double studentTotalScore;      // 学生总得分
}
