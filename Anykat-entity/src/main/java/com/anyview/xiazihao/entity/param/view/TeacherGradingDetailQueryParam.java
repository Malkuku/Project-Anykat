package com.anyview.xiazihao.entity.param.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherGradingDetailQueryParam {
    private Integer teacherId;      // 教师ID(必须)
    private Integer exerciseId;     // 练习ID(必须)
    private String classNames;       // 班级名称(模糊查询)
    private Integer studentId;      // 学生ID
    private String studentName;     // 学生姓名(模糊查询)
    private Integer page = 1;       // 页码(默认1)
    private Integer pageSize = 10;  // 每页条数(默认10)
    private Integer offset = 0;     // 分页偏移量
}
