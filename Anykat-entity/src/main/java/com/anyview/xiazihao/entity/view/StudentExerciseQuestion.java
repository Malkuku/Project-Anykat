package com.anyview.xiazihao.entity.view;

import com.anyview.xiazihao.entity.pojo.StudentAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentExerciseQuestion {
    private Integer exerciseId;          // 练习ID
    private String exerciseName;        // 练习名称
    private Integer courseId;           // 课程ID
    private String courseName;          // 课程名称
    private LocalDateTime startTime;    // 开始时间
    private LocalDateTime endTime;      // 结束时间
    private Integer exerciseStatus;     // 练习状态(0:未开始,1:进行中,2:已结束)

    private Integer questionId;         // 题目ID
    private Integer questionType;       // 题型(0:单选,1:多选,2:简答)
    private String questionDescription; // 题目描述
    private String questionContent;     // 题干内容
    private Integer difficulty;         // 难度(1-5)
    private Integer score;              // 题目分值
    private Integer sortOrder;          // 题目排序号

    private String correctAnswer;       // 正确答案(仅已批改时显示)
    private String answerAnalysis;      // 答案解析(仅已批改时显示)
    private Map<String, String> questionOptions; // 选择题选项(JSON格式)
    private String referenceAnswer;     // 主观题参考答案
    private Integer wordLimit;          // 主观题字数限制

    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间

    private StudentAnswer studentAnswer; // 学生答案
}
