-- 用户表
CREATE TABLE `user` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(50) NOT NULL COMMENT '姓名',
`username` varchar(50) NOT NULL COMMENT '用户名',
`password` varchar(255) NOT NULL COMMENT '密码',
`role`  tinyint(1) NOT NULL COMMENT '身份标识(0:学生,1:老师,2:管理员)',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `idx_username` (`username`)
)  COMMENT='用户表';

-- 班级表
CREATE TABLE `class` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL COMMENT '班级名称',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
)  COMMENT='班级表';

-- 学期表
CREATE TABLE `semester` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(50) NOT NULL COMMENT '学期名称',
`start_time` datetime NOT NULL COMMENT '开始时间',
`end_time` datetime NOT NULL COMMENT '结束时间',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
)  COMMENT='学期表';

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
)  COMMENT='课程表';

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
) COMMENT='基础题目表';
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
) COMMENT='选择题表';
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
) COMMENT='主观题表';


-- 练习表
CREATE TABLE `exercise` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`name` varchar(100) NOT NULL COMMENT '练习名称',
`course_id` int(11) NOT NULL COMMENT '所属课程ID',
`start_time` datetime NOT NULL COMMENT '开始时间',
`end_time` datetime NOT NULL COMMENT '截止时间',
`status`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态(0:未开始,1:进行中,2:已结束)',
`creator_id` int(11) NOT NULL COMMENT '创建者ID',
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
KEY `idx_course` (`course_id`),
KEY `idx_creator` (`creator_id`),
CONSTRAINT `fk_exercise_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`) ON DELETE CASCADE,
CONSTRAINT `fk_exercise_creator` FOREIGN KEY (`creator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
)  COMMENT='练习表';

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
)  COMMENT='学生班级关联表';

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
)  COMMENT='教师课程关联表';

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
)  COMMENT='练习班级关联表';

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
)  COMMENT='练习题目关联表';

-- 学生答题记录表
CREATE TABLE `student_answer` (
`id` int(11) NOT NULL AUTO_INCREMENT,
`student_id` int(11) NOT NULL COMMENT '学生ID',
`exercise_id` int(11) NOT NULL COMMENT '练习ID',
`question_id` int(11) NOT NULL COMMENT '题目ID',
`answer` text COMMENT '提交答案',
`score` int(11) DEFAULT NULL COMMENT '得分',
`correct_status`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '批改状态(0:保存未提交,1:未批改,2:已批改)',
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
)  COMMENT='学生答题记录表';

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
)  COMMENT='提醒表';