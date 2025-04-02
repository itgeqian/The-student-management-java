-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE student_management;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 教师表
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    department_id BIGINT,
    title VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100)
);

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    student_no VARCHAR(50) NOT NULL UNIQUE,
    department_id BIGINT,
    grade VARCHAR(20),
    class_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100)
);

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    credit DECIMAL(3,1),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 课程班级表
CREATE TABLE IF NOT EXISTS course_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    max_student INT DEFAULT 50
);

-- 选课表
CREATE TABLE IF NOT EXISTS course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    select_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 作业表
CREATE TABLE IF NOT EXISTS homework (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_class_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    attachment_url VARCHAR(500),
    deadline DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 作业提交表
CREATE TABLE IF NOT EXISTS homework_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    answer TEXT,
    attachment_url VARCHAR(500),
    score INT,
    comment TEXT,
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 成绩构成表
CREATE TABLE IF NOT EXISTS grade_composition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_class_id BIGINT NOT NULL,
    homework_percentage INT NOT NULL,
    experiment_percentage INT NOT NULL,
    exam_percentage INT NOT NULL
);

-- 期末成绩表
CREATE TABLE IF NOT EXISTS final_grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    exam_score DECIMAL(5,2),
    homework_score DECIMAL(5,2),
    experiment_score DECIMAL(5,2),
    total_score DECIMAL(5,2),
    is_published BOOLEAN DEFAULT FALSE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入管理员账号
INSERT INTO sys_user (username, password, real_name, role) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 'ADMIN');  -- 密码为123456的MD5值
