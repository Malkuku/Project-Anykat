import request from "../../utils/request";

// 查询学生练习信息
export const queryStudentExercisesApi = (params) => request.get('/student-exercises', { params });
