package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.FinalGrade;
import com.student.entity.GradeComposition;

import java.util.List;
import java.util.Map;

public interface GradeService extends IService<FinalGrade> {
    // 基础操作
    FinalGrade createGrade(FinalGrade grade);
    FinalGrade updateGrade(FinalGrade grade);
    void deleteGrade(Long id);
    FinalGrade getGradeById(Long id);
    
    // 成绩查询
    IPage<FinalGrade> getStudentGrades(Long studentId, Page<FinalGrade> page);
    IPage<FinalGrade> getCourseClassGrades(Long courseClassId, Page<FinalGrade> page);
    FinalGrade getStudentFinalGrade(Long studentId, Long courseClassId);
    Page<FinalGrade> getClassFinalGrades(Long courseClassId, int current, int size);
    List<FinalGrade> searchStudentGrades(Long courseClassId, String studentNumber);
    
    // 成绩构成管理
    GradeComposition setGradeComposition(Long courseClassId, Integer homeworkPercentage,
                                         Integer experimentPercentage, Integer examPercentage);
    GradeComposition getGradeComposition(Long courseClassId);
    
    // 考试成绩管理
    void updateExamGrade(Long studentId, Long courseClassId, Integer score);
    void batchUpdateExamGrades(Long courseClassId, List<Map<String, Object>> grades);
    Integer getExamGrade(Long studentId, Long courseClassId);
    
    // 成绩发布管理
    void submitGrades(Long courseClassId);
    void publishGrades(Long courseClassId);
    boolean isGradesPublished(Long courseClassId);
    
    // 统计分析
    Map<String, Object> getGradeStatistics(Long courseClassId);
}
