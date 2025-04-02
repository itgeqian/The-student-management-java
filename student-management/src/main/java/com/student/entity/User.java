package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(condition = SqlCondition.LIKE)
    private String username;

    private String password;

    private String realName;
    
    private String email;
    
    private String phone;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Student studentInfo;

    @TableField(exist = false)
    private Teacher teacherInfo;
}
