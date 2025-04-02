CREATE TABLE final_grade (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_class_id BIGINT NOT NULL COMMENT '课程班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    total_score INT COMMENT '总分',
    obtained_score INT COMMENT '获得的分数',
    submitted_count INT COMMENT '已提交作业数',
    total_count INT COMMENT '总作业数',
    average_score DECIMAL(5,2) COMMENT '平均分',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_class_student (course_class_id, student_id)
) COMMENT='最终成绩表';
