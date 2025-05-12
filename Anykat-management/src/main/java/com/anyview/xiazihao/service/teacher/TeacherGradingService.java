package com.anyview.xiazihao.service.teacher;

import com.anyview.xiazihao.entity.param.view.TeacherGradingDetailQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherGradingDetail;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public interface TeacherGradingService {
    // 练习批改分页查询
    PageResult<TeacherGradingDetail> selectGradingDetailsByPage(TeacherGradingDetailQueryParam param) throws SQLException, FileNotFoundException;
}
