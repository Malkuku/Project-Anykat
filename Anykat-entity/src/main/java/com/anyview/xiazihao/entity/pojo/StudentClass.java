package com.anyview.xiazihao.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentClass {
    private Integer id; // 主键ID
    private Integer studentId; // 学生ID
    private Integer classId; // 班级ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}