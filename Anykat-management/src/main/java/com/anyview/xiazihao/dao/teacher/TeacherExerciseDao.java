package com.anyview.xiazihao.dao.teacher;

import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.pojo.Exercise;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.view.TeacherExercise;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

public interface TeacherExerciseDao {
    // 教师练习列表统计
    Integer selectTeacherExerciseCount(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException;
    //  教师练习列表分页查询
    List<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, SQLException, FileNotFoundException;
    // 获取教师信息
    User selectUserById(Integer id) throws SQLException, FileNotFoundException;
    // 获取学期id
    Integer selectSemesterIdByCourseId(Integer courseId) throws SQLException, FileNotFoundException;
    // 检查练习名
    int checkExerciseName(Integer semesterId, Integer classId, Integer creatorId, String name) throws SQLException, FileNotFoundException;
    // 添加练习
    void addExercise(Exercise exercise) throws SQLException, FileNotFoundException;
    // 添加练习班级
    void addExerciseClasses(Integer exerciseId, List<Integer> classIds) throws SQLException, FileNotFoundException;
    // 添加练习题目
    void addExerciseQuestions(Integer exerciseId, List<Integer> questionIds, List<Integer> questionScores) throws SQLException, FileNotFoundException;
}
