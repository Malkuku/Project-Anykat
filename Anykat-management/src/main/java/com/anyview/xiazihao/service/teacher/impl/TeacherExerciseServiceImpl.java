package com.anyview.xiazihao.service.teacher.impl;

import com.anyview.xiazihao.annotation.KatTransactional;
import com.anyview.xiazihao.containerFactory.annotation.KatAutowired;
import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherExerciseDao;
import com.anyview.xiazihao.entity.exception.IncompleteParameterException;
import com.anyview.xiazihao.entity.exception.NoDatabaseContentException;
import com.anyview.xiazihao.entity.exception.PermissionDeniedException;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.pojo.Exercise;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.result.PageResult;
import com.anyview.xiazihao.entity.view.TeacherExercise;
import com.anyview.xiazihao.service.teacher.TeacherExerciseService;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherExerciseServiceImpl implements TeacherExerciseService {
    @KatAutowired
    private TeacherExerciseDao teacherExerciseDao;

    @Override
    public PageResult<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        Integer total = teacherExerciseDao.selectTeacherExerciseCount(param);
        param.setOffset((param.getPage() - 1) * param.getPageSize());
        List<TeacherExercise> exercises = teacherExerciseDao.selectTeacherExercisesByPage(param);
        return new PageResult<>(total, exercises);
    }

    @Override
    @KatTransactional
    public void addExercise(Exercise exercise) throws SQLException, FileNotFoundException {
        //检查权限
        if(exercise.getCreatorId() == null
        || exercise.getQuestionIds().size() != exercise.getQuestionScores().size()){
            throw new IncompleteParameterException("参数不完整 "+ exercise);
        }
        User user = teacherExerciseDao.selectUserById(exercise.getCreatorId());
        if(user == null || user.getRole() == 0){
            throw new PermissionDeniedException("没有权限");
        }else if(user.getRole() == 1){
            //查看单学期，单班级是否已经存在相同练习名称
            // 获取课程对应的学期ID
            Integer semesterId = teacherExerciseDao.selectSemesterIdByCourseId(exercise.getCourseId());
            for(Integer classId : exercise.getClassIds()) {
                if (teacherExerciseDao.checkExerciseName(semesterId, classId, exercise.getCreatorId(), exercise.getName()) > 0) {
                    throw new PermissionDeniedException("同一学期，同一班级的练习名称不可重复");
                }
            }
        }
        //添加练习表
        teacherExerciseDao.addExercise(exercise);
        Integer exerciseId = JdbcUtils.getLastInsertId();
        //添加练习班级关联表
        teacherExerciseDao.addExerciseClasses(exerciseId, exercise.getClassIds());
        //添加练习题目关联表
        teacherExerciseDao.addExerciseQuestions(exerciseId, exercise.getQuestionIds(), exercise.getQuestionScores());
    }

    @Override
    public void updateExerciseStatus(Integer id, Integer status) throws SQLException, FileNotFoundException {
        //检查练习id是否存在
        if(teacherExerciseDao.checkExerciseId(id) < 1){
            throw new NoDatabaseContentException("练习不存在");
        }
        teacherExerciseDao.updateExerciseStatus(id, status);
    }

    @Override
    public void deleteExercise(Integer id) throws SQLException, FileNotFoundException {
        //检查练习id是否存在
        if(teacherExerciseDao.checkExerciseId(id) < 1){
            throw new NoDatabaseContentException("练习不存在");
        }
        teacherExerciseDao.deleteExercise(id);
    }

    @Override
    public Exercise selectExerciseById(Integer id) throws SQLException, FileNotFoundException {
        if(teacherExerciseDao.checkExerciseId(id) < 1){
            throw new NoDatabaseContentException("练习不存在");
        }
        //查询练习
        Exercise exercise = teacherExerciseDao.selectExerciseById(id);
        //查询班级列表
        exercise.setClassIds(teacherExerciseDao.selectExerciseClassIds(id));
        //查询题目列表
        exercise.setQuestionIds(teacherExerciseDao.selectExerciseQuestionIds(id));
        //查询题目分数
        exercise.setQuestionScores(teacherExerciseDao.selectExerciseQuestionScores(id));
        return exercise;
    }
}
