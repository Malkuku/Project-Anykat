import request from "@/utils/request";

// 查询题目列表
export const queryQuestionsApi = (params) => request.get('/questions', { params });

// 批量删除题目
export const deleteQuestionsApi = (ids) => request.delete('/questions', { params: { ids } });

// 新增题目
export const addQuestionApi = (question) => request.post('/questions', question);

// 根据ID查询题目
export const queryQuestionByIdApi = (id) => request.get(`/questions/${id}`);

// 修改题目
export const updateQuestionApi = (question) => request.put('/questions', question);

// 查询选择题信息
export const queryChoiceByQuestionIdApi = (questionId) => request.get(`/questions/choice/${questionId}`);

// 新增选择题信息
export const addChoiceApi = (choice) => request.post('/questions/choice', choice);

// 修改选择题信息
export const updateChoiceApi = (choice) => request.put('/questions/choice', choice);

// 查询简答题信息
export const querySubjectiveByQuestionIdApi = (questionId) => request.get(`/questions/subjective/${questionId}`);

// 新增简答题信息
export const addSubjectiveApi = (subjective) => request.post('/questions/subjective', subjective);

// 修改简答题信息
export const updateSubjectiveApi = (subjective) => request.put('/questions/subjective', subjective);