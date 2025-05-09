package com.anyview.xiazihao.entity.pojo.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChoiceQuestion {
    private Integer id;            // 主键ID
    private Integer questionId;    // 关联基础题目ID
    private Boolean isMulti = false; // 是否多选题
    private Map<String, String> options; // 选项配置
    private List<String> correctAnswer; // 正确答案
    private String analysis;       // 答案解析
}
