package com.anyview.xiazihao.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourseView {
    private Integer studentId;         // 学生ID
    private String studentName;       // 学生姓名
    private Integer courseId;         // 课程ID
    private String courseName;       // 课程名称
    private Integer semesterId;      // 学期ID
    private String semesterName;     // 学期名称
    private LocalDateTime semesterStart; // 学期开始时间
    private LocalDateTime semesterEnd;   // 学期结束时间
    private Integer exerciseCount;    // 练习数量
}
