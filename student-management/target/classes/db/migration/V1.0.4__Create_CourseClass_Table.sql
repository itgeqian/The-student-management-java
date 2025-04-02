-- 创建课程班级表
DROP TABLE IF EXISTS course_class;
CREATE TABLE course_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    max_student INT NOT NULL DEFAULT 50 COMMENT '最大学生数',
    semester VARCHAR(50) NOT NULL COMMENT '学期',
    class_time VARCHAR(100) COMMENT '上课时间',
    classroom VARCHAR(50) COMMENT '教室',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否激活',
    description TEXT COMMENT '班级描述',
    homework_weight INT NOT NULL DEFAULT 30 COMMENT '作业成绩权重',
    experiment_weight INT NOT NULL DEFAULT 30 COMMENT '实验成绩权重',
    final_exam_weight INT NOT NULL DEFAULT 40 COMMENT '期末考试权重',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程班级表';
