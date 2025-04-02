package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100个字符")
    private String name;

    @Size(max = 500, message = "课程描述长度不能超过500个字符")
    private String description;

    @NotNull(message = "学分不能为空")
    @DecimalMin(value = "0.0", message = "学分不能小于0")
    @DecimalMax(value = "10.0", message = "学分不能大于10")
    private BigDecimal credit;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "院系ID不能为空")
    private Long departmentId;

    @NotBlank(message = "课程代码不能为空")
    @Size(max = 50, message = "课程代码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "课程代码只能包含字母、数字和横杠")
    private String courseCode;

    @Min(value = 1, message = "最大学生数不能小于1")
    @Max(value = 200, message = "最大学生数不能超过200")
    private Integer maxStudents;

    @NotBlank(message = "学期不能为空")
    @Size(max = 20, message = "学期长度不能超过20个字符")
    private String semester;

    @Size(max = 100, message = "上课时间长度不能超过100个字符")
    private String classTime;

    @Size(max = 50, message = "教室长度不能超过50个字符")
    private String classroom;

    private Boolean isActive = true;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Teacher teacher;

    @TableField(exist = false)
    private Department department;

    @TableField(exist = false)
    private List<Student> students;

    @TableField(exist = false)
    private Long studentCount;
}
