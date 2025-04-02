package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("homework_submission")
public class HomeworkSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long homeworkId;

    private Long studentId;

    private String answer;

    private String attachmentUrl;

    private Integer score;

    private String comment;

    private String status = "SUBMITTED";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Homework homework;

    @TableField(exist = false)
    private Student student;

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
