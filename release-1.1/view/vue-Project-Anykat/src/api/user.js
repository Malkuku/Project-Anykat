import request from "../utils/request";

// 分页查询用户信息
export const queryUsersApi = (params) => request.get('/common-users', { params });

/**
 * 用户登录
 * @param {Object} data 登录数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {number} data.role - 身份标识(0:学生,1:老师,2:管理员)
 * @returns {Promise} 包含登录信息的Promise
 */
export const loginApi = (data) => {
    return request.post('/common-users/login', data);
};