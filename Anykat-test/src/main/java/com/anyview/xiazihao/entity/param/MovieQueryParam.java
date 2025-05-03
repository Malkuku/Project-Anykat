package com.anyview.xiazihao.entity.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieQueryParam {
    private String title;          // 电影名称(模糊查询)
    private LocalDate releaseDate;    // 上映日期(格式:YYYY-MM-DD)
    private Integer minDuration;   // 最小时长(分钟)
    private Integer maxDuration;   // 最大时长(分钟)
    private String genre;          // 电影类型
    private Double minRating;  // 最低评分
    private Double maxRating;  // 最高评分
    private Integer status;        // 状态(0-下架,1-上映)
    private Integer page = 1;      // 页码(默认1)
    private Integer pageSize = 10; // 每页条数(默认10)
}
