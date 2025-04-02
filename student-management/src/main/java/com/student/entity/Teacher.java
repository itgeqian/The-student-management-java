package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("teacher")
public class Teacher {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String teacherNo;

    private Long departmentId;

    private String name;

    private String phone;

    private String email;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Department department;
}
