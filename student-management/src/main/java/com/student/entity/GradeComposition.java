package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("grade_composition")
public class GradeComposition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseClassId;

    private Integer homeworkWeight;

    private Integer experimentWeight;

    private Integer finalExamWeight;

    @TableField(exist = false)
    private CourseClass courseClass;
}
