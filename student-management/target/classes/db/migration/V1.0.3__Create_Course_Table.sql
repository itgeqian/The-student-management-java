-- 创建课程表
drop table if exists `course`;
CREATE TABLE IF NOT EXISTS `course` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '课程名称',
    `description` VARCHAR(500) COMMENT '课程描述',
    `credit` DECIMAL(3,1) NOT NULL COMMENT '学分',
    `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
    `department_id` BIGINT NOT NULL COMMENT '院系ID',
    `course_code` VARCHAR(50) NOT NULL COMMENT '课程代码',
    `max_students` INT DEFAULT 50 COMMENT '最大学生数',
    `semester` VARCHAR(20) NOT NULL COMMENT '学期',
    `class_time` VARCHAR(100) COMMENT '上课时间',
    `classroom` VARCHAR(50) COMMENT '教室',
    `is_active` BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`),
    KEY `idx_teacher_id` (`teacher_id`),
    KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 添加一些测试数据
INSERT INTO `course` (`name`, `description`, `credit`, `teacher_id`, `department_id`, `course_code`, `max_students`, `semester`, `class_time`, `classroom`) VALUES 
('Java程序设计', 'Java编程基础与实践', 4.0, 1, 1, 'JAVA-101', 50, '2023-2024-1', '周一 1-2节', 'A101'),
('数据库原理', '数据库设计与SQL编程', 3.0, 2, 1, 'DB-101', 45, '2023-2024-1', '周二 3-4节', 'B202'),
('计算机网络', '网络协议与架构', 3.5, 3, 1, 'NET-101', 60, '2023-2024-1', '周三 5-6节', 'C303');
