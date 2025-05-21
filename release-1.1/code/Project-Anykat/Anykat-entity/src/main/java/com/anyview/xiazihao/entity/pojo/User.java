package com.anyview.xiazihao.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id; // 主键ID
    private String name; // 姓名
    private String username; // 用户名
    private String password; // 密码
    private Integer role; // 身份标识(0:学生,1:老师,2:管理员)
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    private String token; // token
    private String adminToken; // 管理员token
}
