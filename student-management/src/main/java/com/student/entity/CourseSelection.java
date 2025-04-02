package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_selection")
public class CourseSelection {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseClassId;

    private Long studentId;

    // 成绩相关
    private Integer homeworkScore;    // 作业成绩
    private Integer experimentScore;   // 实验成绩
    private Integer finalExamScore;    // 期末考试成绩
    private Integer totalScore;        // 总成绩

    // 选课状态：SELECTED-已选, DROPPED-已退, COMPLETED-已完成
    private String status = "SELECTED";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime selectTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private CourseClass courseClass;

    @TableField(exist = false)
    private Student student;
}
