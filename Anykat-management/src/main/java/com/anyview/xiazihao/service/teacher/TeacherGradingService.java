package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestionDetails;
import com.anyview.xiazihao.entity.view.TeacherGradingQuestions;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface TeacherGradingService {
    // 练习批改分页查询
    PageResult<TeacherGradingDetail> selectGradingDetailsByPage(TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException;

    //  获取简单批改题目信息
    List<TeacherGradingQuestions> selectGradingQuestions(Integer exerciseId, Integer studentId) throws SQLException, FileNotFoundException;

    // 获取详细批改题目信息
    TeacherGradingQuestionDetails selectGradingQuestionDetails(Integer exerciseId, Integer studentId, Integer questionId) throws SQLException, FileNotFoundException;
}
