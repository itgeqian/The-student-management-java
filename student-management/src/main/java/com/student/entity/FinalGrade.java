package com.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("final_grade")
public class FinalGrade {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseClassId;

    private Long studentId;

    private Integer examScore;      // 期末考试成绩

    private Integer homeworkScore;  // 作业平均成绩

    private Integer experimentScore; // 实验平均成绩

    private Integer totalScore;     // 总成绩

    private Boolean isPublished = false;

    private Boolean isSubmitted = false;  // 教师是否已提交成绩

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime publishTime;  // 成绩发布时间

    @TableField(exist = false)
    private CourseClass courseClass;

    @TableField(exist = false)
    private Student student;

    @TableField(exist = false)
    private Integer rank;
    

    // 计算总成绩的方法
    public void calculateTotalScore(Integer examWeight, Integer homeworkWeight, Integer experimentWeight) {
        Integer total = 0;
        
        if (examScore != null && examWeight != null) {
            total = total + (examScore * examWeight);
            //total = total.add(examScore.multiply(examWeight));
        }
        
        if (homeworkScore != null && homeworkWeight != null) {
            total = total + (homeworkScore* (homeworkWeight));
        }
        
        if (experimentScore != null && experimentWeight != null) {
            total = total + (experimentScore * (experimentWeight));
        }
        
        this.totalScore = total;
    }

    // 提交成绩
    public void submit() {
        if (examScore == null || homeworkScore == null || experimentScore == null) {
            throw new IllegalStateException("所有成绩组成部分必须已录入");
        }
        this.isSubmitted = true;
        this.updateTime = LocalDateTime.now();
    }

    // 发布成绩
    public void publish() {
        if (!isSubmitted) {
            throw new IllegalStateException("成绩必须先提交才能发布");
        }
        this.isPublished = true;
        this.publishTime = LocalDateTime.now();
    }

    // 获取成绩等级
    public GradeLevel getGradeLevel() {
        if (totalScore == null) {
            return null;
        }
        
        int score = totalScore.intValue();
        if (score >= 90) return GradeLevel.EXCELLENT;
        if (score >= 80) return GradeLevel.GOOD;
        if (score >= 70) return GradeLevel.FAIR;
        if (score >= 60) return GradeLevel.PASS;
        return GradeLevel.FAIL;
    }
}

enum GradeLevel {
    EXCELLENT(90, 100, "优秀"),
    GOOD(80, 89, "良好"),
    FAIR(70, 79, "中等"),
    PASS(60, 69, "及格"),
    FAIL(0, 59, "不及格");

    private final int min;
    private final int max;
    private final String description;

    GradeLevel(int min, int max, String description) {
        this.min = min;
        this.max = max;
        this.description = description;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getDescription() {
        return description;
    }
}
