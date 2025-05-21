package com.anyview.xiazihao.controller.student;

import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.controller.annotation.KatController;
import com.anyview.xiazihao.controller.annotation.KatRequestMapping;
import com.anyview.xiazihao.controller.annotation.KatRequestParam;
import com.anyview.xiazihao.entity.context.UserContext;
import com.anyview.xiazihao.entity.param.view.StudentExerciseQueryParam;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.StudentExerciseView;
import com.anyview.xiazihao.service.student.StudentExerciseService;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@KatComponent
@KatController
@KatRequestMapping(path = "/student-exercises")
public class StudentExerciseController {
    @KatAutowired
    private StudentExerciseService studentExerciseService;

    // 学生练习信息分页查询
    @KatRequestMapping(path = "", method = "GET")
    public PageResult<StudentExerciseView> selectStudentExercisesByPage(
            @KatRequestParam("param") StudentExerciseQueryParam param) throws SQLException, FileNotFoundException {
        if(UserContext.isAuthOpen()){
            try{
                param.setStudentId(UserContext.getUser().getId());
            }finally{
                UserContext.clear();
            }
        }
        return studentExerciseService.selectStudentExercisesByPage(param);
    }
}
