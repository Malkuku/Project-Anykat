import request from "@/utils/request";

// 查询练习题目列表及答题情况
export const queryQuestionsApi = (params) => request.get('/student-answers/questions', { params });

// 查询特定答题记录
export const queryAnswerApi = (params) => request.get('/student-answers', { params });

// 批量提交答题记录
export const submitAnswersApi = (data) => request.post('/student-answers', data);