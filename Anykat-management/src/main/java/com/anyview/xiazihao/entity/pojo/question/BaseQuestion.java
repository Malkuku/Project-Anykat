package com.anyview.xiazihao.entity.pojo.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseQuestion {
    private Integer id;             // 主键ID
    private Integer type;          // 题型(0:单选,1:多选,2:填空)
    private String description;    // 题目描述
    private String content;        // 题干内容
    private Integer difficulty = 1;// 难度(1-5)
    private Integer score = 0;     // 分值
    private Integer creatorId;     // 创建人ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
