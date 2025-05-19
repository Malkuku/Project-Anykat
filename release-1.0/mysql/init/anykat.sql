create database IF NOT EXISTS anykat;

use anykat;


###################### 创建表单 ###########################
DROP TABLE IF EXISTS `student_class`;
DROP TABLE IF EXISTS `student_answer`;
DROP TABLE IF EXISTS `exercise_class`;
DROP TABLE IF EXISTS `exercise_question`;
DROP TABLE IF EXISTS `teacher_course`;
DROP TABLE IF EXISTS `choice_question`;
DROP TABLE IF EXISTS `subjective_question`;
DROP TABLE IF EXISTS `base_question`;
DROP TABLE IF EXISTS `exercise`;
DROP TABLE IF EXISTS `class`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `semester`;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(50) NOT NULL COMMENT '姓名',
`username` varchar(50) NOT NULL COMMENT '用户名',
`password` varchar(255) NOT NULL COMMENT '密码',
`role`  tinyint(2) NOT NULL COMMENT '身份标识(0:学生,1:老师,2:管理员)',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 班级表
CREATE TABLE `class` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL COMMENT '班级名称',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 学期表
CREATE TABLE `semester` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(50) NOT NULL COMMENT '学期名称',
`start_time` datetime NOT NULL COMMENT '开始时间',
`end_time` datetime NOT NULL COMMENT '结束时间',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学期表';

-- 课程表
CREATE TABLE `course` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL COMMENT '课程名称',
`semester_id` int(11) NOT NULL COMMENT '所属学期ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
KEY `idx_semester` (`semester_id`),
CONSTRAINT `fk_course_semester` FOREIGN KEY (`semester_id`) REFERENCES `semester` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 题目表
CREATE TABLE `base_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type`  tinyint NOT NULL COMMENT '题型(0:单选,1:多选,2:简答)',
  `description` varchar(255) COMMENT '题目描述',
  `content` text NOT NULL COMMENT '题干内容',
  `difficulty`  tinyint DEFAULT 1 COMMENT '难度(1-5)',
  `score` int NOT NULL DEFAULT 0 COMMENT '默认分值',
  `creator_id` int NOT NULL COMMENT '创建人',
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_type` (`type`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='基础题目表';
-- 选择题扩展表
CREATE TABLE `choice_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL COMMENT '关联基础题目ID',
  `is_multi` boolean DEFAULT FALSE COMMENT '是否多选题',
  `options` json NOT NULL COMMENT '选项配置',
  `correct_answer` varchar(20) NOT NULL COMMENT '正确答案',
  `analysis` text COMMENT '答案解析',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question` (`question_id`),
  CONSTRAINT `fk_cq_question` FOREIGN KEY (`question_id`)
    REFERENCES `base_question` (`id`) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='选择题表';
-- 选项JSON示例：
-- {
--   "A": "TCP协议",
--   "B": "HTTP协议",
--   "C": "FTP协议",
--   "D": "UDP协议"
-- }
-- 答案示例："A"
-- 简答题扩展表
CREATE TABLE `subjective_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `question_id` int NOT NULL COMMENT '关联基础题目ID',
  `reference_answer` text NOT NULL COMMENT '参考答案',
  `word_limit` int COMMENT '字数限制',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question` (`question_id`),
  CONSTRAINT `fk_sq_question` FOREIGN KEY (`question_id`)
    REFERENCES `base_question` (`id`) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='主观题表';


-- 练习表
CREATE TABLE `exercise` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL COMMENT '练习名称',
`course_id` int(11) NOT NULL COMMENT '所属课程ID',
`start_time` datetime NOT NULL COMMENT '开始时间',
`end_time` datetime NOT NULL COMMENT '截止时间',
`status`  tinyint(2) NOT NULL DEFAULT '0' COMMENT '状态(0:未开始,1:进行中,2:已结束)',
`creator_id` int(11) NOT NULL COMMENT '创建者ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
KEY `idx_course` (`course_id`),
KEY `idx_creator` (`creator_id`),
CONSTRAINT `fk_exercise_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_exercise_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习表';

-- 学生班级关联表
CREATE TABLE `student_class` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`student_id` int(11) NOT NULL COMMENT '学生ID',
`class_id` int(11) NOT NULL COMMENT '班级ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_student_class` (`student_id`,`class_id`),
KEY `idx_class` (`class_id`),
CONSTRAINT `fk_sc_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_sc_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci  COMMENT='学生班级关联表';

-- 教师课程关联表
CREATE TABLE `teacher_course` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`teacher_id` int(11) NOT NULL COMMENT '教师ID',
`course_id` int(11) NOT NULL COMMENT '课程ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_teacher_course` (`teacher_id`,`course_id`),
KEY `idx_course` (`course_id`),
CONSTRAINT `fk_tc_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_tc_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师课程关联表';

-- 练习班级关联表
CREATE TABLE `exercise_class` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`exercise_id` int(11) NOT NULL COMMENT '练习ID',
`class_id` int(11) NOT NULL COMMENT '班级ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_exercise_class` (`exercise_id`,`class_id`),
KEY `idx_class` (`class_id`),
CONSTRAINT `fk_ec_class` FOREIGN KEY (`class_id`) REFERENCES `class` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_ec_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习班级关联表';

-- 练习题目关联表
CREATE TABLE `exercise_question` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`exercise_id` int(11) NOT NULL COMMENT '练习ID',
`question_id` int(11) NOT NULL COMMENT '题目ID',
`score` int(11) NOT NULL DEFAULT 0 COMMENT '题目分值',
`sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '题目排序号',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_exercise_question` (`exercise_id`,`question_id`),
KEY `idx_question` (`question_id`),
CONSTRAINT `fk_eq_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_eq_question` FOREIGN KEY (`question_id`) REFERENCES `base_question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习题目关联表';

-- 学生答题记录表
CREATE TABLE `student_answer` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`student_id` int(11) NOT NULL COMMENT '学生ID',
`exercise_id` int(11) NOT NULL COMMENT '练习ID',
`question_id` int(11) NOT NULL COMMENT '题目ID',
`answer` text COMMENT '提交答案',
`score` int(11) DEFAULT NULL COMMENT '得分',
`correct_status`  tinyint(2) NOT NULL DEFAULT '0' COMMENT '批改状态(0:保存未提交,1:未批改,2:已批改)',
`correct_comment` varchar(255) DEFAULT NULL COMMENT '批改备注',
`correct_time` datetime DEFAULT NULL COMMENT '批改时间',
`submit_time` datetime DEFAULT NULL COMMENT '提交时间',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_student_exercise_question` (`student_id`,`exercise_id`,`question_id`),
KEY `idx_exercise` (`exercise_id`),
KEY `idx_question` (`question_id`),
CONSTRAINT `fk_sa_exercise` FOREIGN KEY (`exercise_id`) REFERENCES `exercise` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_sa_question` FOREIGN KEY (`question_id`) REFERENCES `base_question` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_sa_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题记录表';

-- 提醒表
CREATE TABLE `notification` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`target_user_id` int(11) NOT NULL COMMENT '目标用户ID',
`sender_id` int(11) NOT NULL COMMENT '发起者ID',
`effective_time` datetime NOT NULL COMMENT '生效时间',
`content` text NOT NULL COMMENT '提醒内容',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
KEY `idx_target_user` (`target_user_id`),
KEY `idx_sender` (`sender_id`),
CONSTRAINT `fk_notification_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_notification_target` FOREIGN KEY (`target_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒表';



############################## 设置视图 #############################################

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

-- 完成数据统计
CREATE OR REPLACE VIEW v_student_course_progress AS
SELECT
    sc.student_id,
    c.id AS course_id,
    s.id AS semester_id,
    e.id AS exercise_id,
    (SELECT COUNT(DISTINCT e2.id)
     FROM exercise e2
     JOIN exercise_class ec2 ON ec2.exercise_id = e2.id
     JOIN student_class sc2 ON sc2.class_id = ec2.class_id
     WHERE e2.course_id = c.id AND sc2.student_id = sc.student_id) AS total_exercises,
    COUNT(DISTINCT eq.question_id) AS total_questions,
    SUM(eq.score) AS total_score,
    COUNT(DISTINCT sa.question_id) AS completed_questions,
    SUM(CASE WHEN sa.correct_status > 0 THEN eq.score ELSE 0 END) AS completed_score,
    e.status AS exercise_status
FROM
    course c
JOIN semester s ON c.semester_id = s.id
JOIN exercise e ON e.course_id = c.id
JOIN exercise_question eq ON eq.exercise_id = e.id
JOIN exercise_class ec ON ec.exercise_id = e.id
JOIN student_class sc ON sc.class_id = ec.class_id
JOIN user u ON sc.student_id = u.id
LEFT JOIN student_answer sa ON sa.exercise_id = e.id
                         AND sa.question_id = eq.question_id
                         AND sa.student_id = sc.student_id
                         AND sa.correct_status > 0
GROUP BY
    sc.student_id, c.id, s.id, e.id, e.status;


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
    COUNT(DISTINCT CASE WHEN sa.correct_status IN (1, 2) THEN sa.question_id END) AS completed_question_count,
    SUM(COALESCE(CASE WHEN sa.correct_status IN (1, 2) THEN sa.score ELSE 0 END, 0)) AS student_total_score
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

-- 纯粹的题目列表版本
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
    -- 正确答案（总是显示，不依赖学生答题状态）
    CASE
        WHEN bq.type IN (0, 1) THEN cq.correct_answer  -- 选择题答案
        WHEN bq.type = 2 THEN sq.reference_answer     -- 主观题参考答案
        ELSE NULL
    END AS correct_answer,
    -- 答案解析
    CASE
        WHEN bq.type IN (0, 1) THEN cq.analysis       -- 选择题解析
        ELSE NULL
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



############################## 设置测试数据 #############################################
-- 删除所有表数据（按照外键依赖顺序）
-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 使用TRUNCATE清空表（更快且重置自增ID）
TRUNCATE TABLE student_answer;
TRUNCATE TABLE exercise_question;
TRUNCATE TABLE exercise_class;
TRUNCATE TABLE notification;
TRUNCATE TABLE teacher_course;
TRUNCATE TABLE student_class;
TRUNCATE TABLE exercise;
TRUNCATE TABLE course;
TRUNCATE TABLE choice_question;
TRUNCATE TABLE subjective_question;
TRUNCATE TABLE base_question;
TRUNCATE TABLE semester;
TRUNCATE TABLE class;
TRUNCATE TABLE user;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

#用户表
-- 管理员
INSERT INTO `user` (`name`, `username`, `password`, `role`) VALUES
('张管理员', 'admin1', 'kat1234', 2),
('李系统', 'admin2', 'kat1234', 2);

-- 教师
INSERT INTO `user` (`name`, `username`, `password`, `role`) VALUES
('王老师', 'teacher1', 'kat1234', 1),
('赵教授', 'teacher2', 'kat1234', 1),
('钱讲师', 'teacher3', 'kat1234', 1),
('孙导师', 'teacher4', 'kat1234', 1),
('周副教授', 'teacher5', 'kat1234', 1);

-- 学生
INSERT INTO `user` (`name`, `username`, `password`, `role`) VALUES
('吴同学', 'student1', 'kat1234', 0),
('郑学生', 'student2', 'kat1234', 0),
('冯学员', 'student3', 'kat1234', 0),
('陈学子', 'student4', 'kat1234', 0),
('褚同学', 'student5', 'kat1234', 0),
('卫学生', 'student6', 'kat1234', 0),
('蒋学员', 'student7', 'kat1234', 0),
('沈学子', 'student8', 'kat1234', 0),
('韩同学', 'student9', 'kat1234', 0),
('杨学生', 'student10', 'kat1234', 0),
('朱学员', 'student11', 'kat1234', 0),
('秦学子', 'student12', 'kat1234', 0),
('尤同学', 'student13', 'kat1234', 0);


#班级表
INSERT INTO `class` (`name`) VALUES
('计算机科学与技术1班'),
('计算机科学与技术2班'),
('软件工程1班'),
('软件工程2班'),
('人工智能1班'),
('数据科学1班'),
('网络安全1班'),
('物联网工程1班'),
('电子信息工程1班'),
('自动化1班'),
('数学与应用数学1班'),
('物理1班'),
('化学1班'),
('生物科学1班'),
('英语1班'),
('汉语言文学1班'),
('历史学1班'),
('哲学1班'),
('经济学1班'),
('管理学1班');

#学期表
INSERT INTO `semester` (`name`, `start_time`, `end_time`) VALUES
('2023年春季学期', '2023-02-20 00:00:00', '2023-06-30 23:59:59'),
('2023年秋季学期', '2023-09-01 00:00:00', '2024-01-20 23:59:59'),
('2024年春季学期', '2024-02-19 00:00:00', '2024-06-28 23:59:59'),
('2024年秋季学期', '2024-09-02 00:00:00', '2025-01-18 23:59:59'),
('2025年春季学期', '2025-02-17 00:00:00', '2025-06-27 23:59:59'),
('2025年秋季学期', '2025-08-31 00:00:00', '2026-01-17 23:59:59'),
('2026年春季学期', '2026-02-16 00:00:00', '2026-06-26 23:59:59'),
('2026年秋季学期', '2026-08-30 00:00:00', '2027-01-16 23:59:59'),
('2027年春季学期', '2027-02-15 00:00:00', '2027-06-25 23:59:59'),
('2027年秋季学期', '2027-08-29 00:00:00', '2028-01-15 23:59:59'),
('2028年春季学期', '2028-02-14 00:00:00', '2028-06-23 23:59:59'),
('2028年秋季学期', '2028-08-27 00:00:00', '2029-01-13 23:59:59'),
('2029年春季学期', '2029-02-12 00:00:00', '2029-06-22 23:59:59'),
('2029年秋季学期', '2029-08-26 00:00:00', '2030-01-12 23:59:59'),
('2030年春季学期', '2030-02-11 00:00:00', '2030-06-21 23:59:59'),
('2030年秋季学期', '2030-08-25 00:00:00', '2031-01-11 23:59:59'),
('2031年春季学期', '2031-02-10 00:00:00', '2031-06-20 23:59:59'),
('2031年秋季学期', '2031-08-24 00:00:00', '2032-01-10 23:59:59'),
('2032年春季学期', '2032-02-09 00:00:00', '2032-06-19 23:59:59'),
('2032年秋季学期', '2032-08-23 00:00:00', '2033-01-09 23:59:59');

#课程表
INSERT INTO `course` (`name`, `semester_id`) VALUES
('数据结构', 1),
('算法设计', 1),
('数据库系统', 2),
('操作系统', 2),
('计算机网络', 3),
('计算机组成原理', 3),
('软件工程', 4),
('人工智能导论', 4),
('机器学习', 5),
('深度学习', 5),
('大数据技术', 6),
('云计算', 6),
('网络安全基础', 7),
('密码学', 7),
('物联网技术', 8),
('嵌入式系统', 8),
('数字信号处理', 9),
('自动控制原理', 9),
('高等数学', 10),
('线性代数', 10);

#题目表
INSERT INTO `base_question` (`type`, `description`, `content`, `difficulty`, `score`, `creator_id`) VALUES
-- 单选题(1-10)
(0, '数据结构基础', '以下哪种数据结构是先进先出的?', 1, 10, 1),
(0, '算法复杂度', '快速排序的平均时间复杂度是?', 3, 10, 1),
(0, '操作系统', '以下哪个不是操作系统的功能?', 2, 10, 2),
(0, '组成原理', 'CPU的主要组成部分不包括?', 2, 10, 2),
(0, '网络协议', 'HTTP协议默认端口号是?', 1, 5, 3),
(0, '数据库', '以下哪个不是关系型数据库?', 2, 10, 3),
(0, '编程语言', 'Java是哪种类型的语言?', 1, 5, 4),
(0, '人工智能', '以下哪个不是机器学习算法?', 1, 5, 4),
(0, '大数据', 'Hadoop的核心组件不包括?', 2, 10, 5),
(0, '云计算', '以下哪项不是云计算的特点?', 2, 10, 5),

-- 多选题(11-15)
(1, '数据结构', '以下哪些是线性数据结构?', 3, 15, 1),
(1, '算法', '以下哪些排序算法是稳定的?', 4, 15, 2),
(1, '操作系统', '以下哪些是进程调度算法?', 3, 15, 3),
(1, '网络', '以下哪些是TCP协议的特点?', 3, 15, 4),
(1, '数据库', '以下哪些是NoSQL数据库?', 3, 15, 5),

-- 简答题(16-20)
(2, '软件工程', '请简述敏捷开发的主要特点', 3, 20, 1),
(2, '网络协议', '请解释TCP三次握手的过程', 4, 20, 2),
(2, '数据库', '请简述数据库三大范式的主要内容', 3, 20, 3),
(2, '编程基础', '请描述面向对象编程的三大特性', 2, 15, 4),
(2, '操作系统', '请解释死锁的四个必要条件', 4, 20, 5);

INSERT INTO `choice_question` (`question_id`, `is_multi`, `options`, `correct_answer`, `analysis`) VALUES
-- 单选题(1-10)
(1, FALSE, '{"A":"队列","B":"栈","C":"树","D":"图"}', 'A', '队列是先进先出的线性数据结构'),
(2, FALSE, '{"A":"O(n)","B":"O(nlogn)","C":"O(n^2)","D":"O(logn)"}', 'B', '快速排序平均时间复杂度为O(nlogn)'),
(3, FALSE, '{"A":"内存管理","B":"文件管理","C":"编译程序","D":"进程调度"}', 'C', '编译程序是编译器功能，不属于操作系统核心功能'),
(4, FALSE, '{"A":"运算器","B":"控制器","C":"存储器","D":"寄存器"}', 'C', '存储器属于存储系统，不是CPU的组成部分'),
(5, FALSE, '{"A":"80","B":"443","C":"3306","D":"21"}', 'A', 'HTTP默认端口是80，HTTPS是443'),
(6, FALSE, '{"A":"MySQL","B":"MongoDB","C":"PostgreSQL","D":"Oracle"}', 'B', 'MongoDB是文档型NoSQL数据库'),
(7, FALSE, '{"A":"编译型","B":"解释型","C":"混合型","D":"脚本语言"}', 'C', 'Java是先编译为字节码再解释执行'),
(8, FALSE, '{"A":"KNN","B":"SVM","C":"HTTP","D":"决策树"}', 'C', 'HTTP是协议不是算法'),
(9, FALSE, '{"A":"HDFS","B":"MapReduce","C":"YARN","D":"MySQL"}', 'D', 'MySQL是关系型数据库'),
(10, FALSE, '{"A":"按需自助服务","B":"广泛的网络访问","C":"资源池化","D":"固定容量"}', 'D', '云计算特点是弹性可扩展'),

-- 多选题(11-15)
(11, TRUE, '{"A":"数组","B":"链表","C":"栈","D":"二叉树"}', 'A,B,C', '二叉树是非线性数据结构'),
(12, TRUE, '{"A":"冒泡排序","B":"快速排序","C":"归并排序","D":"堆排序"}', 'A,C', '归并排序和冒泡排序是稳定的'),
(13, TRUE, '{"A":"FIFO","B":"SJF","C":"RR","D":"DFS"}', 'A,B,C', 'DFS是深度优先搜索算法'),
(14, TRUE, '{"A":"面向连接","B":"可靠传输","C":"无拥塞控制","D":"全双工"}', 'A,B,D', 'TCP有拥塞控制机制'),
(15, TRUE, '{"A":"MongoDB","B":"Redis","C":"HBase","D":"MySQL"}', 'A,B,C', 'MySQL是关系型数据库');

-- 简答题(16-20)
INSERT INTO `subjective_question` (`question_id`, `reference_answer`, `word_limit`) VALUES
(16, '1. 迭代开发\n2. 用户参与\n3. 适应变化\n4. 交付可工作软件\n5. 面对面沟通', 200),
(17, '第一次握手：客户端发送SYN=1,seq=x\n第二次握手：服务器发送SYN=1,ACK=1,seq=y,ack=x+1\n第三次握手：客户端发送ACK=1,seq=x+1,ack=y+1', 300),
(18, '第一范式：每个列都是不可分割的原子数据项\n第二范式：满足第一范式，并且非主属性完全依赖于主键\n第三范式：满足第二范式，并且非主属性不传递依赖于主键', 250),
(19, '1. 封装：隐藏对象内部实现细节\n2. 继承：子类继承父类特性\n3. 多态：同一操作作用于不同对象产生不同行为', 150),
(20, '1. 互斥条件\n2. 请求与保持条件\n3. 不剥夺条件\n4. 循环等待条件', 200);


#练习表
INSERT INTO `exercise` (`name`, `course_id`, `start_time`, `end_time`, `status`, `creator_id`) VALUES
('数据结构第一次作业', 1, '2023-03-01 00:00:00', '2023-03-07 23:59:59', 2, 3),
('算法设计期中测试', 2, '2023-04-15 00:00:00', '2023-04-17 23:59:59', 2, 3),
('数据库系统实验一', 3, '2023-10-10 00:00:00', '2023-10-17 23:59:59', 2, 4),
('操作系统期末考试', 4, '2024-01-10 00:00:00', '2024-01-12 23:59:59', 2, 4),
('计算机网络平时作业', 5, '2024-04-01 00:00:00', '2026-04-08 23:59:59', 1, 5),
('组成原理实验二', 6, '2024-05-10 00:00:00', '2026-05-17 23:59:59', 1, 5),
('软件工程小组项目', 7, '2024-11-01 00:00:00', '2026-11-30 23:59:59', 1, 6),
('人工智能导论作业', 8, '2025-03-15 00:00:00', '2026-03-22 23:59:59', 1, 6),
('机器学习实验三', 9, '2025-05-10 00:00:00', '2026-05-17 23:59:59', 1, 7),
('深度学习大作业', 10, '2025-06-01 00:00:00', '2025-06-15 23:59:59', 0, 7),
('大数据技术期中考试', 11, '2026-04-10 00:00:00', '2026-04-12 23:59:59', 0, 8),
('云计算实验四', 12, '2026-05-20 00:00:00', '2026-05-27 23:59:59', 0, 8),
('网络安全基础作业', 13, '2027-03-01 00:00:00', '2027-03-08 23:59:59', 0, 9),
('密码学期末考试', 14, '2027-06-10 00:00:00', '2027-06-12 23:59:59', 0, 9),
('物联网技术实验一', 15, '2028-04-05 00:00:00', '2028-04-12 23:59:59', 0, 10),
('嵌入式系统作业', 16, '2028-05-15 00:00:00', '2028-05-22 23:59:59', 0, 10),
('数字信号处理期中', 17, '2029-04-01 00:00:00', '2029-04-03 23:59:59', 0, 11),
('自动控制原理实验', 18, '2029-05-10 00:00:00', '2029-05-17 23:59:59', 0, 11),
('高等数学作业一', 19, '2030-03-15 00:00:00', '2030-03-22 23:59:59', 0, 12),
('线性代数期末考试', 20, '2030-06-10 00:00:00', '2030-06-12 23:59:59', 0, 12);


#学生班级关联表
INSERT INTO `student_class` (`student_id`, `class_id`) VALUES
(7, 1), (8, 1), (9, 1), (10, 1),
(11, 2), (12, 2), (13, 2), (14, 2),
(15, 3), (16, 3), (7, 3), (8, 3),
(9, 4), (10, 4), (11, 4), (12, 4),
(13, 5), (14, 5), (15, 5), (16, 5);

# 教师课程关联表
INSERT INTO `teacher_course` (`teacher_id`, `course_id`) VALUES
(3, 1), (3, 2),
(4, 3), (4, 4),
(5, 5), (5, 6),
(6, 7), (6, 8),
(7, 9), (7, 10),
(8, 11), (8, 12),
(9, 13), (9, 14),
(10, 15), (10, 16),
(11, 17), (11, 18),
(12, 19), (12, 20);

#练习班级关联表
INSERT INTO `exercise_class` (`exercise_id`, `class_id`) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3),
(3, 2), (3, 4),
(4, 3), (4, 5),
(5, 4), (5, 6),
(6, 5), (6, 7),
(7, 6), (7, 8),
(8, 7), (8, 9),
(9, 8), (9, 10),
(10, 9), (10, 11);

#练习题目关联表
INSERT INTO `exercise_question` (`exercise_id`, `question_id`, `sort_order`, `score`) VALUES
-- 练习1的题目（分值分别为5和10）
(1, 1, 1, 5), (1, 2, 2, 10),
-- 练习2的题目（分值分别为8和12）
(2, 3, 1, 8), (2, 4, 2, 12),
-- 练习3的题目（分值均为10）
(3, 5, 1, 10), (3, 6, 2, 10),
-- 练习4的题目（分值分别为5和15）
(4, 7, 1, 5), (4, 8, 2, 15),
-- 练习5的题目（分值分别为10和20）
(5, 9, 1, 10), (5, 10, 2, 20),
-- 练习6的题目（分值均为15）
(6, 11, 1, 15), (6, 12, 2, 15),
-- 练习7的题目（分值分别为5和25）
(7, 13, 1, 5), (7, 14, 2, 25),
-- 练习8的题目（分值分别为10和10）
(8, 15, 1, 10), (8, 16, 2, 10),
-- 练习9的题目（分值分别为15和5）
(9, 17, 1, 15), (9, 18, 2, 5),
-- 练习10的题目（分值均为20）
(10, 19, 1, 20), (10, 20, 2, 20);

#学生答题记录表
-- 插入正确的测试数据
INSERT INTO `student_answer` (`student_id`, `exercise_id`, `question_id`, `answer`, `score`, `correct_status`, `correct_comment`, `correct_time`, `submit_time`) VALUES
-- 已批改的答案(状态2)
(7, 1, 1, 'A', 10, 2, '回答正确', '2023-03-05 10:00:00', '2023-03-03 15:30:00'),
(7, 1, 2, 'B', 10, 2, '回答正确', '2023-03-05 10:00:00', '2023-03-03 15:30:00'),
(8, 1, 1, 'A', 10, 2, '回答正确', '2023-03-05 10:00:00', '2023-03-04 14:20:00'),
(8, 1, 2, 'C', 5, 2, '回答错误', '2023-03-05 10:00:00', '2023-03-04 14:20:00'),

-- 已提交未批改的答案(状态1)
(9, 2, 3, '第一范式...', NULL, 1, NULL, NULL, '2023-04-15 16:45:00'),
(9, 2, 4, 'C', NULL, 1, NULL, NULL, '2023-04-15 16:45:00'),
(10, 2, 3, '三大范式是...', NULL, 1, NULL, NULL, '2023-04-16 08:30:00'),
(10, 2, 4, 'D', NULL, 1, NULL, NULL, '2023-04-16 08:30:00'),

-- 保存未提交的答案(状态0)
(11, 3, 5, '三次握手是...', NULL, 0, NULL, NULL, NULL),
(11, 3, 6, 'C', NULL, 0, NULL, NULL, NULL),
(12, 3, 5, '客户端发送...', NULL, 0, NULL, NULL, NULL),
(12, 3, 6, 'A', NULL, 0, NULL, NULL, NULL),

-- 混合状态的答案
(13, 4, 7, '敏捷开发...', 8, 2, '回答正确', '2024-01-11 16:00:00', '2024-01-10 15:10:00'),
(13, 4, 8, 'C', NULL, 1, NULL, NULL, '2024-01-10 15:10:00'),
(14, 4, 7, '特点是...', NULL, 0, NULL, NULL, NULL),
(14, 4, 8, 'B', 5, 2, '回答错误', '2024-01-11 16:00:00', '2024-01-11 10:30:00'),

-- 最新练习的答案
(15, 5, 9, '反向传播...', 9, 2, '回答详细', '2024-04-05 11:00:00', '2024-04-03 14:45:00'),
(15, 5, 10, 'D', NULL, 1, NULL, NULL, '2024-04-03 14:45:00'),
(16, 5, 9, '通过误差...', NULL, 0, NULL, NULL, NULL),
(16, 5, 10, 'A', NULL, 0, NULL, NULL, NULL);


#提醒表
INSERT INTO `notification` (`target_user_id`, `sender_id`, `effective_time`, `content`) VALUES
(7, 3, '2023-03-01 08:00:00', '数据结构作业已发布，请在3月7日前完成'),
(8, 3, '2023-03-01 08:00:00', '数据结构作业已发布，请在3月7日前完成'),
(9, 3, '2023-04-14 08:00:00', '算法设计期中测试将于明天开始'),
(10, 3, '2023-04-14 08:00:00', '算法设计期中测试将于明天开始'),
(11, 4, '2023-10-09 08:00:00', '数据库系统实验一已发布，请按时完成'),
(12, 4, '2023-10-09 08:00:00', '数据库系统实验一已发布，请按时完成'),
(13, 4, '2024-01-09 08:00:00', '操作系统期末考试安排已发布'),
(14, 4, '2024-01-09 08:00:00', '操作系统期末考试安排已发布'),
(15, 5, '2024-03-31 08:00:00', '计算机网络平时作业已发布'),
(16, 5, '2024-03-31 08:00:00', '计算机网络平时作业已发布'),
(7, 5, '2024-05-09 08:00:00', '组成原理实验二即将开始'),
(8, 5, '2024-05-09 08:00:00', '组成原理实验二即将开始'),
(9, 6, '2024-10-31 08:00:00', '软件工程小组项目即将开始'),
(10, 6, '2024-10-31 08:00:00', '软件工程小组项目即将开始'),
(11, 6, '2025-03-14 08:00:00', '人工智能导论作业已发布'),
(12, 6, '2025-03-14 08:00:00', '人工智能导论作业已发布'),
(13, 7, '2025-05-09 08:00:00', '机器学习实验三即将开始'),
(14, 7, '2025-05-09 08:00:00', '机器学习实验三即将开始'),
(15, 7, '2025-05-31 08:00:00', '深度学习大作业即将开始'),
(16, 7, '2025-05-31 08:00:00', '深度学习大作业即将开始');

#补充测试数据

# 新增练习
INSERT INTO `exercise` (`name`, `course_id`, `start_time`, `end_time`, `status`, `creator_id`) VALUES
-- 正在进行中的练习(状态1)
('数据结构期末复习', 1, '2025-05-15 00:00:00', '2025-05-22 23:59:59', 1, 3),
('算法设计实验三', 2, '2025-05-18 00:00:00', '2025-05-25 23:59:59', 1, 3),
('数据库系统大作业', 3, '2025-05-20 00:00:00', '2025-06-05 23:59:59', 1, 4),

-- 即将开始的练习(状态0)
('操作系统期中考试', 4, '2025-05-25 00:00:00', '2025-05-27 23:59:59', 0, 4),
('计算机网络实验四', 5, '2025-05-28 00:00:00', '2025-06-04 23:59:59', 0, 5),
('组成原理期末复习', 6, '2025-06-01 00:00:00', '2025-06-08 23:59:59', 0, 5),
('软件工程期末考试', 7, '2025-06-10 00:00:00', '2025-06-12 23:59:59', 0, 6),
('人工智能导论实验', 8, '2025-06-15 00:00:00', '2025-06-22 23:59:59', 0, 6),
('机器学习期末项目', 9, '2025-06-18 00:00:00', '2025-06-25 23:59:59', 0, 7),
('深度学习实验五', 10, '2025-06-20 00:00:00', '2025-06-30 23:59:59', 0, 7);

# 新增练习班级关联
INSERT INTO `exercise_class` (`exercise_id`, `class_id`) VALUES
(21, 1), (21, 2),  -- 数据结构期末复习
(22, 1), (22, 3),  -- 算法设计实验三
(23, 2), (23, 4),  -- 数据库系统大作业
(24, 3), (24, 5),  -- 操作系统期中考试
(25, 4), (25, 6),  -- 计算机网络实验四
(26, 5), (26, 7),  -- 组成原理期末复习
(27, 6), (27, 8),  -- 软件工程期末考试
(28, 7), (28, 9),  -- 人工智能导论实验
(29, 8), (29, 10), -- 机器学习期末项目
(30, 9), (30, 11); -- 深度学习实验五

# 新增练习题目关联
INSERT INTO `exercise_question` (`exercise_id`, `question_id`, `sort_order`, `score`) VALUES
-- 数据结构期末复习(2题)
(21, 1, 1, 10), (21, 11, 2, 15),
-- 算法设计实验三(2题)
(22, 2, 1, 10), (22, 12, 2, 20),
-- 数据库系统大作业(3题)
(23, 5, 1, 10), (23, 6, 2, 15), (23, 15, 3, 15),
-- 操作系统期中考试(2题)
(24, 3, 1, 15), (24, 13, 2, 20),
-- 计算机网络实验四(2题)
(25, 4, 1, 10), (25, 14, 2, 15),
-- 组成原理期末复习(3题)
(26, 7, 1, 10), (26, 8, 2, 10), (26, 17, 3, 20),
-- 软件工程期末考试(2题)
(27, 9, 1, 15), (27, 16, 2, 25),
-- 人工智能导论实验(2题)
(28, 10, 1, 10), (28, 18, 2, 20),
-- 机器学习期末项目(3题)
(29, 19, 1, 15), (29, 20, 2, 20), (29, 16, 3, 15),
-- 深度学习实验五(2题)
(30, 17, 1, 20), (30, 18, 2, 20);

# 新增学生答题记录(部分已完成)
INSERT INTO `student_answer` (`student_id`, `exercise_id`, `question_id`, `answer`, `score`, `correct_status`, `correct_comment`, `correct_time`, `submit_time`) VALUES
-- 数据结构期末复习(已提交部分)
(7, 21, 1, 'A', 10, 2, '回答正确', '2025-05-16 14:00:00', '2025-05-16 10:30:00'),
(7, 21, 11, 'A,B,C', 15, 2, '回答正确', '2025-05-16 14:00:00', '2025-05-16 10:30:00'),
(8, 21, 1, 'B', 0, 2, '回答错误', '2025-05-17 09:00:00', '2025-05-17 08:45:00'),
(8, 21, 11, 'A,B', 10, 2, '部分正确', '2025-05-17 09:00:00', '2025-05-17 08:45:00'),

-- 算法设计实验三(已提交未批改)
(9, 22, 2, 'B', NULL, 1, NULL, NULL, '2025-05-19 15:20:00'),
(9, 22, 12, 'A,C', NULL, 1, NULL, NULL, '2025-05-19 15:20:00'),
(10, 22, 2, 'C', NULL, 1, NULL, NULL, '2025-05-19 16:30:00'),

-- 数据库系统大作业(已保存未提交)
(11, 23, 5, '80', NULL, 0, NULL, NULL, NULL),
(12, 23, 6, 'B', NULL, 0, NULL, NULL, NULL);

# 新增通知
INSERT INTO `notification` (`target_user_id`, `sender_id`, `effective_time`, `content`) VALUES
-- 给所有学生的近期练习通知
(7, 3, '2025-05-18 08:00:00', '数据结构期末复习进行中，截止5月22日'),
(8, 3, '2025-05-18 08:00:00', '数据结构期末复习进行中，截止5月22日'),
(9, 3, '2025-05-17 08:00:00', '算法设计实验三已开始，请及时完成'),
(10, 3, '2025-05-17 08:00:00', '算法设计实验三已开始，请及时完成'),
(11, 4, '2025-05-19 08:00:00', '数据库系统大作业明天开始，请做好准备'),
(12, 4, '2025-05-19 08:00:00', '数据库系统大作业明天开始，请做好准备'),
(13, 4, '2025-05-24 08:00:00', '操作系统期中考试即将开始，请复习第1-5章'),
(14, 4, '2025-05-24 08:00:00', '操作系统期中考试即将开始，请复习第1-5章'),
(15, 5, '2025-05-27 08:00:00', '下周将进行计算机网络实验四'),
(16, 5, '2025-05-27 08:00:00', '下周将进行计算机网络实验四');




