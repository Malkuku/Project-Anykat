package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseQuestion {
    private Integer id; // 主键ID
    private Integer exerciseId; // 练习ID
    private Integer questionId; // 题目ID
    private Integer sortOrder = 0; // 题目排序号
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}