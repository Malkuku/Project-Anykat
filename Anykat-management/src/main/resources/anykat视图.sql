-- 学生课程视图
CREATE OR REPLACE VIEW v_student_courses AS
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

-- 学生练习视图
CREATE OR REPLACE VIEW v_student_exercises AS
SELECT
    e.id AS exercise_id,
    e.name AS exercise_name,
    e.course_id,
    c.name AS course_name,
    e.start_time,
    e.end_time,
    e.status,
    e.creator_id,
    u.name AS creator_name,
    s.id AS semester_id,
    s.name AS semester_name,
    sc.student_id,
    COUNT(DISTINCT eq.question_id) AS total_question_count,
    SUM(eq.score) AS total_exercise_score,
    COUNT(DISTINCT sa.question_id) AS completed_question_count,
    SUM(COALESCE(sa.score, 0)) AS student_total_score
FROM
    exercise e
JOIN
    course c ON e.course_id = c.id
JOIN
    semester s ON c.semester_id = s.id
JOIN
    exercise_class ec ON e.id = ec.exercise_id
JOIN
    student_class sc ON ec.class_id = sc.class_id
JOIN
    user u ON e.creator_id = u.id
LEFT JOIN
    exercise_question eq ON e.id = eq.exercise_id
LEFT JOIN
    base_question bq ON eq.question_id = bq.id
LEFT JOIN
    student_answer sa ON e.id = sa.exercise_id
                     AND sc.student_id = sa.student_id
                     AND eq.question_id = sa.question_id
GROUP BY
    e.id, e.name, e.course_id, c.name, e.start_time, e.end_time,
    e.status, e.creator_id, u.name, s.id, s.name, sc.student_id;


-- 学生查看练习题目列表
CREATE OR REPLACE VIEW v_student_exercise_questions AS
SELECT
    eq.exercise_id,
    e.name AS exercise_name,
    e.course_id,
    c.name AS course_name,
    e.start_time,
    e.end_time,
    e.status AS exercise_status,
    bq.id AS question_id,
    bq.type AS question_type,
    bq.description AS question_description,
    bq.content AS question_content,
    bq.difficulty,
    eq.score,
    eq.sort_order,
    sa.student_id,
    u.name AS student_name,
    sa.answer AS student_answer,
    sa.score AS student_score,
    sa.correct_status,
    sa.correct_comment,
    sa.submit_time,
    -- 根据批改状态决定是否显示正确答案
    CASE
        WHEN sa.correct_status = 2 THEN cq.correct_answer  -- 已批改显示答案
        WHEN sa.correct_status = 1 THEN NULL               -- 未批改不显示
        ELSE NULL                                         -- 其他情况不显示
    END AS correct_answer,
    -- 根据批改状态决定是否显示答案解析
    CASE
        WHEN sa.correct_status = 2 THEN cq.analysis       -- 已批改显示解析
        WHEN sa.correct_status = 1 THEN NULL              -- 未批改不显示
        ELSE NULL                                        -- 其他情况不显示
    END AS answer_analysis,
    -- 选择题选项
    CASE
        WHEN bq.type IN (0, 1) THEN cq.options
        ELSE NULL
    END AS question_options,
    -- 主观题参考信息
    CASE
        WHEN bq.type = 2 THEN sq.reference_answer
        ELSE NULL
    END AS reference_answer,
    CASE
        WHEN bq.type = 2 THEN sq.word_limit
        ELSE NULL
    END AS word_limit,
    eq.created_at,
    eq.updated_at
FROM
    exercise_question eq
JOIN
    exercise e ON eq.exercise_id = e.id
JOIN
    course c ON e.course_id = c.id
JOIN
    base_question bq ON eq.question_id = bq.id
LEFT JOIN
    student_answer sa ON eq.exercise_id = sa.exercise_id
                     AND eq.question_id = sa.question_id
LEFT JOIN
    user u ON sa.student_id = u.id
LEFT JOIN
    choice_question cq ON bq.id = cq.question_id AND bq.type IN (0, 1)
LEFT JOIN
    subjective_question sq ON bq.id = sq.question_id AND bq.type = 2;


-- 教师练习列表查询
CREATE OR REPLACE VIEW `v_teacher_exercises` AS
SELECT
    e.id AS exercise_id,
    e.name AS exercise_name,
    e.start_time,
    e.end_time,
    e.status,
    e.created_at AS exercise_created_at,
    c.id AS course_id,
    c.name AS course_name,
    s.id AS semester_id,
    s.name AS semester_name,
    s.start_time AS semester_start_time,
    s.end_time AS semester_end_time,
    cl.id AS class_id,
    cl.name AS class_name,
    u.id AS teacher_id,
    u.name AS teacher_name,
    COUNT(DISTINCT eq.question_id) AS question_count,
    COUNT(DISTINCT ec.class_id) AS class_count,
    (SELECT COUNT(DISTINCT sa.student_id)
     FROM student_answer sa
     JOIN student_class sc ON sa.student_id = sc.student_id
     JOIN exercise_class ec ON ec.class_id = sc.class_id AND ec.exercise_id = e.id
     WHERE sa.exercise_id = e.id AND sa.correct_status > 0) AS submitted_student_count
FROM
    exercise e
JOIN
    course c ON e.course_id = c.id
JOIN
    semester s ON c.semester_id = s.id
JOIN
    teacher_course tc ON tc.course_id = c.id
JOIN
    user u ON tc.teacher_id = u.id
LEFT JOIN
    exercise_question eq ON eq.exercise_id = e.id
LEFT JOIN
    exercise_class ec ON ec.exercise_id = e.id
LEFT JOIN
    class cl ON ec.class_id = cl.id
GROUP BY
    e.id, c.id, s.id, cl.id, u.id;


CREATE OR REPLACE VIEW `v_teacher_grading_details` AS
SELECT
    tc.teacher_id AS teacher_id,
    (SELECT name FROM user WHERE id = tc.teacher_id) AS teacher_name,
    e.id AS exercise_id,
    e.name AS exercise_name,
    c.id AS class_id,
    c.name AS class_name,
    u.id AS student_id,
    u.name AS student_name,

    -- 题目完成情况统计
    COUNT(DISTINCT sa.question_id) AS answered_questions,
    (SELECT COUNT(*) FROM exercise_question eq WHERE eq.exercise_id = e.id) AS total_questions,

    -- 批改状态统计
    SUM(CASE WHEN sa.correct_status = 0 THEN 1 ELSE 0 END) AS saved_unsubmitted_count,
    SUM(CASE WHEN sa.correct_status = 1 THEN 1 ELSE 0 END) AS submitted_uncorrected_count,
    SUM(CASE WHEN sa.correct_status = 2 THEN 1 ELSE 0 END) AS corrected_count,

    -- 分数统计
    COALESCE(SUM(sa.score), 0) AS current_score,
    (SELECT SUM(eq.score)
    FROM exercise_question eq
    WHERE eq.exercise_id = e.id) AS max_score,

    -- 提交和批改时间
    MAX(sa.submit_time) AS last_submit_time,
    MAX(sa.correct_time) AS last_correct_time

FROM
    exercise e
JOIN
    teacher_course tc ON tc.course_id = e.course_id
JOIN
    exercise_class ec ON ec.exercise_id = e.id
JOIN
    class c ON ec.class_id = c.id
JOIN
    student_class sc ON sc.class_id = c.id
JOIN
    user u ON sc.student_id = u.id AND u.role = 0
LEFT JOIN
    student_answer sa ON sa.student_id = u.id AND sa.exercise_id = e.id
GROUP BY
    tc.teacher_id, e.id, c.id, u.id;


-- 简单批改信息
CREATE OR REPLACE VIEW v_teacher_grading_questions AS
SELECT
    sa.exercise_id,
    sa.student_id,
    sa.question_id,
    bq.content AS question_name,
    sa.correct_status,
    sa.score AS grading_score
FROM
    student_answer sa
JOIN
    base_question bq ON sa.question_id = bq.id
JOIN
    exercise_question eq ON sa.exercise_id = eq.exercise_id
                       AND sa.question_id = eq.question_id
ORDER BY
    eq.sort_order;


-- 学生答题详情视图（教师用）
CREATE OR REPLACE VIEW v_teacher_grading_question_details AS
SELECT
    sa.exercise_id,
    sa.student_id,
    sa.question_id,
    bq.content AS question_content,
    bq.type AS question_type,
    eq.score AS max_score,
    sa.answer AS student_answer,
    sa.score AS current_score,
    CASE
        WHEN bq.type = 2 THEN (SELECT reference_answer FROM subjective_question WHERE question_id = bq.id)
        WHEN bq.type IN (0,1) THEN (SELECT correct_answer FROM choice_question WHERE question_id = bq.id)
    END AS reference_answer,
    sa.id AS answer_id
FROM
    student_answer sa
JOIN
    base_question bq ON sa.question_id = bq.id
JOIN
    exercise_question eq ON sa.exercise_id = eq.exercise_id
                       AND sa.question_id = eq.question_id;