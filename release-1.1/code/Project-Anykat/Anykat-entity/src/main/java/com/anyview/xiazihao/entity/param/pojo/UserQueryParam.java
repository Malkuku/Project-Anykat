package com.anyview.xiazihao.entity.param.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQueryParam {
    private String username; // 用户名
    private String name; // 姓名
    private String password; //  密码
    private Integer role; //  身份标识(0:学生,1:老师,2:管理员)
    private Integer page; // 页码
    private Integer pageSize; //  页大小
    private Integer offset; // 偏移量
}
