package com.anyview.xiazihao.entity.param.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SemesterQueryParam {
    private String name;         // 学期名称(模糊查询)
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private Integer page = 1;     // 页码(默认1)
    private Integer pageSize = 10;// 每页条数(默认10)
    private Integer offset = 0;   // 偏移量
}