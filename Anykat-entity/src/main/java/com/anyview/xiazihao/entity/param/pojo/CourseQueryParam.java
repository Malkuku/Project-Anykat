package com.anyview.xiazihao.entity.param.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseQueryParam {
    private String name;         // 课程名称(模糊查询)
    private Integer semesterId;   // 所属学期ID
    private Integer page = 1;     // 页码(默认1)
    private Integer pageSize = 10;// 每页条数(默认10)
    private Integer offset = 0;   // 偏移量
}