package com.anyview.xiazihao.entity.param.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherExerciseQueryParam {
    private Integer teacherId;          // 教师ID(必须)
    private Integer semesterId;        // 学期ID
    private String exerciseName;       // 练习名称(模糊查询)
    private String courseName;         // 课程名称(模糊查询)
    private String className;          // 班级名称(模糊查询)
    private LocalDateTime startTime;   // 开始时间范围起始
    private LocalDateTime endTime;     // 结束时间范围截止
    private Integer status;            // 状态(0:未开始,1:进行中,2:已结束)
    private Integer minQuestionCount;  // 最少题目数
    private Integer maxQuestionCount;  // 最多题目数
    private Integer page = 1;          // 页码(默认1)
    private Integer pageSize = 10;     // 每页条数(默认10)
}
