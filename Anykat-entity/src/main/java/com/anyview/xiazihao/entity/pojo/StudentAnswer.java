package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAnswer {
    private Integer id; // 主键ID
    private Integer studentId; // 学生ID
    private Integer exerciseId; // 练习ID
    private Integer questionId; // 题目ID
    private String answer; // 提交答案
    private Integer score; // 得分
    private Integer correctStatus = 0; // 批改状态(0:保存未提交,1:未批改,2:已批改)
    private String correctComment; // 批改备注
    private LocalDateTime correctTime; // 批改时间
    private LocalDateTime submitTime; // 提交时间
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
