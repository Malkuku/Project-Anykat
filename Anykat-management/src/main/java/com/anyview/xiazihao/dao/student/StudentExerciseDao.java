package com.anyview.xiazihao.dao.student;

import com.anyview.xiazihao.entity.param.view.StudentExerciseQueryParam;
import com.anyview.xiazihao.entity.view.StudentExerciseView;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface StudentExerciseDao {
    //  查询学生某门课程的练习
    Integer selectStudentExerciseCount(StudentExerciseQueryParam param) throws FileNotFoundException, SQLException;
    //  分页查询学生某门课程的练习
    List<StudentExerciseView> selectStudentExercisesByPage(StudentExerciseQueryParam param) throws SQLException, FileNotFoundException;
}
