package com.anyview.xiazihao.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionQueryParam {
    private Integer type;          // 题型(0:单选,1:多选,2:简答)
    private String description;    // 题目描述(模糊查询)
    private String content;        // 题干内容(模糊查询)
    private Integer minDifficulty; // 最小难度(1-5)
    private Integer maxDifficulty; // 最大难度(1-5)
    private Integer minScore;     // 最小分值
    private Integer maxScore;     // 最大分值
    private Integer creatorId;     // 创建人ID
    private Integer page = 1;      // 页码(默认1)
    private Integer pageSize = 10; // 每页条数(默认10)
    private Integer offset = 0;    // 偏移量
}
