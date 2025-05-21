import request from "../../utils/request";

// 分页查询练习列表
export const queryExerciseListApi = (params) => request.get('/teacher-exercises', { params });

// 新增练习
export const addExerciseApi = (data) => request.post('/teacher-exercises', data);

// 根据ID查询练习详情
export const queryExerciseByIdApi = (id) => request.get(`/teacher-exercises/${id}`);

// 修改练习
export const updateExerciseApi = (data) => request.put('/teacher-exercises', data);

// 删除练习
export const deleteExerciseByIdApi = (id) => request.delete(`/teacher-exercises/${id}`);

// 修改练习状态
export const updateExerciseStatusApi = (data) => request.put('/teacher-exercises/status', data);