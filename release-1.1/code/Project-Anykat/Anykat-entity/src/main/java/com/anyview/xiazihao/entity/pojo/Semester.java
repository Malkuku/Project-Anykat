package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Semester {
    private Integer id; // 主键ID
    private String name; // 学期名称
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime; // 结束时间
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
