package com.anyview.xiazihao.entity.pojo.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectiveQuestion {
    private Integer id;            // 主键ID
    private Integer questionId;    // 关联基础题目ID
    private String referenceAnswer; // 参考答案
    private Integer wordLimit;     // 字数限制
}
