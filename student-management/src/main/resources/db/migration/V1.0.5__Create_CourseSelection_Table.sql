-- 创建课程选择表
DROP TABLE IF EXISTS course_selection;
CREATE TABLE course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_class_id BIGINT NOT NULL COMMENT '课程班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    homework_score INT COMMENT '作业成绩',
    experiment_score INT COMMENT '实验成绩',
    final_exam_score INT COMMENT '期末考试成绩',
    total_score INT COMMENT '总成绩',
    status VARCHAR(20) NOT NULL DEFAULT 'SELECTED' COMMENT '选课状态：SELECTED-已选, DROPPED-已退, COMPLETED-已完成',
    select_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_class_student (course_class_id, student_id) COMMENT '班级学生唯一索引',
    KEY idx_student (student_id) COMMENT '学生索引',
    KEY idx_class (course_class_id) COMMENT '班级索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程选择表';
