package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("student")
public class Student {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String studentNo;

    private String name;

    private Long departmentId;

    private String grade;

    private String className;

    private String phone;

    private String email;

    /**
     * 性别：1 男  / 2 女
     */
    private Integer gender;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Department department;
}
