import request from "@/utils/request";

/**
 * 用户信息查询
 * @param {Object} params 查询参数
 * @param {string} [params.name] - 用户姓名(模糊查询)
 * @param {string} [params.username] - 用户名(模糊查询)
 * @param {number} [params.role] - 身份标识(0:学生,1:老师,2:管理员)
 * @param {number} [params.page=1] - 页码(默认1)
 * @param {number} [params.pageSize=10] - 每页条数(默认10)
 * @returns {Promise} 包含用户列表的Promise
 */
export const queryUsersApi = (params) => {
    return request.get('/common-users', {
        params: {
            page: 1,
            pageSize: 10,
            ...params
        }
    });
};

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