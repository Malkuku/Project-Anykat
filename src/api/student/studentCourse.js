import request from "@/utils/request";

// 分页查询学生课程信息
export const queryStudentCoursesApi = (params) => request.get('/student-courses', { params });
