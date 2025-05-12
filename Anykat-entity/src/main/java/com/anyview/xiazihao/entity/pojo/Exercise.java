package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exercise {
    private Integer id; // 主键ID
    private String name; // 练习名称
    private Integer courseId; // 所属课程ID
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime; // 截止时间
    private Integer status = 0; // 状态(0:未开始,1:进行中,2:已结束)
    private Integer creatorId; // 创建者ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    private List<Integer> classIds; //班级列表
    private List<Integer> questionIds; //问题列表
    private List<Integer> questionScores; //分数列表
}
