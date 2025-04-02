package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("course_class")
public class CourseClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long courseId;
    
    private Long teacherId;
    
    private String className;
    
    private Integer maxStudent = 50;
    
    private String semester;
    
    private String classTime;
    
    private String classroom;
    
    private Boolean isActive = true;
    
    private String description;

    // 成绩构成比例
    private Integer homeworkWeight = 30;    // 默认30%
    
    private Integer experimentWeight = 30;   // 默认30%
    
    private Integer finalExamWeight = 40;    // 默认40%
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Course course;

    @TableField(exist = false)
    private Teacher teacher;

    @TableField(exist = false)
    private List<Student> students;

    @TableField(exist = false)
    private List<CourseSelection> courseSelections;

    @TableField(exist = false)
    private Integer submissionCount;
}
