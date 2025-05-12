package com.anyview.xiazihao.dao.teacher.impl;

import com.anyview.xiazihao.containerFactory.annotation.KatComponent;
import com.anyview.xiazihao.containerFactory.annotation.KatSingleton;
import com.anyview.xiazihao.dao.teacher.TeacherExerciseDao;
import com.anyview.xiazihao.entity.param.view.TeacherExerciseQueryParam;
import com.anyview.xiazihao.entity.pojo.Exercise;
import com.anyview.xiazihao.entity.pojo.User;
import com.anyview.xiazihao.entity.view.TeacherExercise;
import com.anyview.xiazihao.utils.JdbcUtils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@KatComponent
@KatSingleton
public class TeacherExerciseDaoImpl implements TeacherExerciseDao {
    @Override
    public Integer selectTeacherExerciseCount(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT COUNT(*)
            FROM v_teacher_exercises
            WHERE teacher_id = #{teacherId}
                AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
                AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
                AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
                AND (#{className} IS NULL OR class_name LIKE CONCAT('%', #{className}, '%'))
                AND (#{startTime} IS NULL OR start_time >= #{startTime})
                AND (#{endTime} IS NULL OR end_time <= #{endTime})
                AND (#{status} IS NULL OR status = #{status})
                AND (#{minQuestionCount} IS NULL OR question_count >= #{minQuestionCount})
                AND (#{maxQuestionCount} IS NULL OR question_count <= #{maxQuestionCount})
            """;
        List<Integer> total = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                param
        );
        return total.get(0);
    }

    @Override
    public List<TeacherExercise> selectTeacherExercisesByPage(TeacherExerciseQueryParam param) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM v_teacher_exercises
            WHERE teacher_id = #{teacherId}
                AND (#{semesterId} IS NULL OR semester_id = #{semesterId})
                AND (#{exerciseName} IS NULL OR exercise_name LIKE CONCAT('%', #{exerciseName}, '%'))
                AND (#{courseName} IS NULL OR course_name LIKE CONCAT('%', #{courseName}, '%'))
                AND (#{className} IS NULL OR class_name LIKE CONCAT('%', #{className}, '%'))
                AND (#{startTime} IS NULL OR start_time >= #{startTime})
                AND (#{endTime} IS NULL OR end_time <= #{endTime})
                AND (#{status} IS NULL OR status = #{status})
                AND (#{minQuestionCount} IS NULL OR question_count >= #{minQuestionCount})
                AND (#{maxQuestionCount} IS NULL OR question_count <= #{maxQuestionCount})
            ORDER BY start_time DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """;
        return JdbcUtils.executeQuery(
                sql,
                TeacherExercise.class,
                param
        );
    }

    @Override
    public User selectUserById(Integer id) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT *
            FROM user
            WHERE id = ?
            """;
        List<User> userList = JdbcUtils.executeQuery(
                sql,
                User.class,
                id
            );
        return userList.isEmpty() ? null : userList.get(0);
    }

    @Override
    public Integer selectSemesterIdByCourseId(Integer courseId) throws SQLException, FileNotFoundException {
        String sql = """
            SELECT semester_id
            FROM course
            WHERE id = ?
            """;
        List<Integer> semesterIds = JdbcUtils.executeQuery(
                sql,
                Integer.class,
                courseId
        );
        return semesterIds.isEmpty() ? null : semesterIds.get(0);
    }

    @Override
    public int checkExerciseName(Integer semesterId, Integer classId, Integer creatorId, String name) throws SQLException, FileNotFoundException {
        String sql = """
                SELECT COUNT(*)
                FROM `exercise` e
                JOIN `course` c ON e.course_id = c.id
                JOIN `exercise_class` ec ON e.id = ec.exercise_id
                WHERE c.semester_id = ?
                  AND ec.class_id = ?
                  AND e.creator_id = ?
                  AND e.name = ?;
            """;
        return JdbcUtils.executeQuery(
                sql,
                Integer.class,
                semesterId, classId, creatorId, name
        ).get(0);
    }

    @Override
    public void addExercise(Exercise exercise) throws SQLException, FileNotFoundException {
        String sql = """
            INSERT INTO `exercise` (name, course_id, start_time, end_time, status, creator_id)
            VALUES(#{name},#{courseId},#{startTime},#{endTime},#{status},#{creatorId})
            """;
        JdbcUtils.executeUpdate(
                sql,
                exercise
        );
    }

    @Override
    public void addExerciseClasses(Integer exerciseId, List<Integer> classIds) throws SQLException, FileNotFoundException {
        String sql = "INSERT INTO `exercise_class` (exercise_id, class_id)"+
                "VALUES " +
                String.join(",", classIds.stream().map(id -> "(?,?)").toArray(String[]::new));
        List<Integer> params = new ArrayList<>();
        for (Integer classId : classIds) {
            params.add(exerciseId);
            params.add(classId);
        }
        JdbcUtils.executeUpdate(
                sql,
                params.toArray()
        );
    }

    @Override
    public void addExerciseQuestions(Integer exerciseId, List<Integer> questionIds, List<Integer> questionScores) throws SQLException, FileNotFoundException {
        String sql ="INSERT INTO `exercise_question` (exercise_id,question_id,score)"+
                "VALUES " +
                String.join(",", questionIds.stream().map(id -> "(?,?,?)").toArray(String[]::new));
        List<Integer> params = new ArrayList<>();
        for(int i = 0; i < questionIds.size(); i++){
            params.add(exerciseId);
            params.add(questionIds.get(i));
            params.add(questionScores.get(i));
        }
        JdbcUtils.executeUpdate(
                sql,
                params.toArray()
        );
    }
}
