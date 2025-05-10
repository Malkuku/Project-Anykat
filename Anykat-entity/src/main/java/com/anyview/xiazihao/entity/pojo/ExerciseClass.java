package com.anyview.xiazihao.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseClass {
    private Integer id; // 主键ID
    private Integer exerciseId; // 练习ID
    private Integer classId; // 班级ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}