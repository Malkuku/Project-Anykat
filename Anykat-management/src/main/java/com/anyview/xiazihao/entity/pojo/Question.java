package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private Integer id; // 主键ID
    private Integer type; // 题目类型(0:选择题,1:简答题)
    private String description; // 题目描述
    private String content; // 题干内容
    private String answer; // 标准答案
    private Integer difficulty = 1; // 难度(1-5)
    private Integer score = 0; // 题目分值
    private Integer creatorId; // 创建者ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
