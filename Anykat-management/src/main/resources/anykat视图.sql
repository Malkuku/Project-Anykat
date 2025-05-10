-- 学生课程视图
CREATE VIEW v_student_courses AS
SELECT
    u.id AS student_id,
    u.name AS student_name,
    c.id AS course_id,
    c.name AS course_name,
    s.id AS semester_id,
    s.name AS semester_name,
    s.start_time AS semester_start,
    s.end_time AS semester_end,
    COUNT(DISTINCT e.id) AS exercise_count
FROM
    user u
        JOIN
    student_class sc ON u.id = sc.student_id
        JOIN
    exercise_class ec ON sc.class_id = ec.class_id
        JOIN
    exercise e ON ec.exercise_id = e.id
        JOIN
    course c ON e.course_id = c.id
        JOIN
    semester s ON c.semester_id = s.id
WHERE
    u.role = 0  -- 学生角色
GROUP BY
    u.id, c.id, s.id;