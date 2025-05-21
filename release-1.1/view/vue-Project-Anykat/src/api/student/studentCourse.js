import request from "../../utils/request";

// 查询学生课程进度
export const queryStudentCourseProgressApi =  (params) => request.get('/student-courses/progress', { params });

// 分页查询学生课程信息
export const queryStudentCoursesApi = (params) => request.get('/student-courses', { params });
