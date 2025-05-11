package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherExercise {
    private Integer exerciseId;          // 练习ID
    private String exerciseName;         // 练习名称
    private LocalDateTime startTime;     // 开始时间
    private LocalDateTime endTime;       // 结束时间
    private Integer status;              // 状态
    private LocalDateTime exerciseCreatedAt; // 练习创建时间

    private Integer courseId;            // 课程ID
    private String courseName;           // 课程名称

    private Integer semesterId;          // 学期ID
    private String semesterName;         // 学期名称
    private LocalDateTime semesterStartTime; // 学期开始时间
    private LocalDateTime semesterEndTime;   // 学期结束时间

    private Integer classId;            // 班级ID
    private String className;           // 班级名称

    private Integer teacherId;          // 教师ID
    private String teacherName;         // 教师姓名

    private Integer questionCount;      // 题目数量
    private Integer classCount;         // 参与班级数量
    private Integer submittedStudentCount; // 已提交学生数量
}