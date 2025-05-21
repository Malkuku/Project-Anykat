import request from "../../utils/request";

// 批改详情查询
export const queryGradingDetailsApi = (params) => request.get('/teacher-grading/details', { params });

// 简单批改信息查询
export const queryGradingQuestionsApi = (params) => request.get('/teacher-grading/questions', { params });

// 学生答题详情查询
export const queryQuestionDetailsApi = (params) => request.get('/teacher-grading/question-details', { params });

// 修改批改状态
export const updateCorrectionApi = (data) => request.put('/teacher-grading/correction', data);