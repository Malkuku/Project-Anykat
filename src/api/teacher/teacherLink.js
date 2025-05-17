import request from '@/utils/request';

// 根据教师ID查询关联班级
export const queryTeacherClassesApi = (params) => {
    return request({
        url: `/teacher-link/classes`,
        method: 'get',
        params
    });
};

// 根据教师ID查询关联课程
export const queryTeacherCoursesApi = (params) => {
    return request({
        url: '/teacher-link/courses',
        method: 'get',
        params
    });
};

// 根据教师ID查询关联学期
export const queryTeacherSemestersApi = (teacherId) => {
    return request({
        url: `/teacher-link/semesters/${teacherId}`,
        method: 'get'
    });
};