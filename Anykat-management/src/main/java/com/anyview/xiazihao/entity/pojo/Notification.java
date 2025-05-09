package com.anyview.xiazihao.entity.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Integer id; // 主键ID
    private Integer targetUserId; // 目标用户ID
    private Integer senderId; // 发起者ID
    private LocalDateTime effectiveTime; // 生效时间
    private String content; // 提醒内容
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}