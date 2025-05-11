package com.anyview.xiazihao.entity.param.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentExerciseQueryParam {
    private Integer courseId;            // 课程ID(必填)
    private Integer studentId;           // 学生ID(必填)
    private String exerciseName;        // 练习名称(模糊查询)
    private Integer semesterId;         // 学期ID
    private String semesterName;       // 学期名称(模糊查询)
    private Double minTotalScore;     // 最低练习总分
    private Double maxTotalScore;     // 最高练习总分
    private Double minStudentScore;   // 最低学生得分
    private Double maxStudentScore;   // 最高学生得分
    private Integer status;           // 练习状态
    private LocalDateTime startTimeBegin; // 练习开始时间范围起始
    private LocalDateTime startTimeEnd;   // 练习开始时间范围结束
    private LocalDateTime endTimeBegin;   // 练习结束时间范围起始
    private LocalDateTime endTimeEnd;     // 练习结束时间范围结束
    private Integer page = 1;         // 页码(默认1)
    private Integer pageSize = 10;    // 每页条数(默认10)
    private Integer offset = 0;       // 偏移量

}
