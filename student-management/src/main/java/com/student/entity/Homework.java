package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseClassId;

    private String title;

    private String content;

    private String attachmentUrl;

    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 课程班级信息
     */
    @TableField(exist = false)
    private CourseClass courseClass;

    @TableField(exist = false)
    private Boolean needSubmit;  // 是否需要提交

    @TableField(exist = false)
    private Boolean isDeadlinePassed;

    @TableField(exist = false)
    private String submissionContent;

    @TableField(exist = false)
    private String submissionAttachment;

    @TableField(exist = false)
    private String submissionStatus;

    @TableField(exist = false)
    private Integer submissionScore;

    @TableField(exist = false)
    private String submissionComment;

    @TableField(exist = false)
    private LocalDateTime submissionTime;

    @TableField(exist = false)
    private LocalDateTime submissionUpdateTime;

    /**
     * 作业类型
     * "HOMEWORK", "平时作业";"EXPERIMENT", "实验作业";"EXAM", "期末考试"
     *                         EXPERIMENT(),
     *                         EXAM();
     */
    private String type;

    /**
     * 已提交人数
     */
    @TableField(exist = false)
    private Integer submitCount;

    /**
     * 总人数
     */
    @TableField(exist = false)
    private Integer totalCount;
}
