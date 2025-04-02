package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assignment_submission")
public class AssignmentSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assignmentId;

    private Long studentId;

    private String answer;

    private String attachmentUrl;

    private Integer score;

    private String comment;

    private String status = "SUBMITTED";  // SUBMITTED, GRADED, RETURNED

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime gradeTime;

    @TableField(exist = false)
    private Student student;

    // 判断是否可以修改
    public boolean canEdit() {
        return status.equals("RETURNED");
    }

    // 设置成绩
    public void grade(Integer score, String comment) {
        this.score = score;
        this.comment = comment;
        this.status = "GRADED";
        this.gradeTime = LocalDateTime.now();
    }

    // 退回作业
    public void returnSubmission(String comment) {
        this.comment = comment;
        this.status = "RETURNED";
        this.updateTime = LocalDateTime.now();
    }
}
