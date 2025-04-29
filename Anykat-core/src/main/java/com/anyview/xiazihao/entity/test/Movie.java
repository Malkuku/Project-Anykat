package com.anyview.xiazihao.entity.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {
    private Integer id; // 主键ID
    private String title; // 电影名称
    private String posterUrl; // 海报URL
    private LocalDate releaseDate; // 上映日期
    private Integer duration; // 时长(分钟)
    private String genre; // 类型
    private Double rating; // 评分
    private Integer status = 1; // 状态:0-下架 1-上映
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
