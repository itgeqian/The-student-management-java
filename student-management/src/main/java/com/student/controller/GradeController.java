package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.FinalGrade;
import com.student.entity.GradeComposition;
import com.student.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    // 成绩构成管理
    @PostMapping("/composition/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<GradeComposition> setGradeComposition(
            @PathVariable Long courseClassId,
            @RequestBody GradeComposition composition) {
        return Result.success(gradeService.setGradeComposition(
                courseClassId, 
                composition.getHomeworkWeight(),
                composition.getExperimentWeight(),
                composition.getFinalExamWeight()));
    }

    @GetMapping("/composition/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<GradeComposition> getGradeComposition(@PathVariable Long courseClassId) {
        return Result.success(gradeService.getGradeComposition(courseClassId));
    }

    // 末考成绩管理
    @PostMapping("/exam/{studentId}/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> updateExamGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId,
            @RequestParam Integer score) {
        gradeService.updateExamGrade(studentId, courseClassId, score);
        return Result.success();
    }

    @PostMapping("/exam/batch/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> batchUpdateExamGrades(
            @PathVariable Long courseClassId,
            @RequestBody Map<String, List<Map<String, Object>>> request) {
        List<Map<String, Object>> grades = request.get("grades");
        gradeService.batchUpdateExamGrades(courseClassId, grades);
        return Result.success();
    }

    @GetMapping("/exam/{studentId}/{courseClassId}")
    public Result<Integer> getExamGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        return Result.success(gradeService.getExamGrade(studentId, courseClassId));
    }

    // 最终成绩管理
    @GetMapping("/student/{studentId}/{courseClassId}")
    public Result<FinalGrade> getStudentFinalGrade(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        return Result.success(gradeService.getStudentFinalGrade(studentId, courseClassId));
    }

    @GetMapping("/class/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Page<FinalGrade>> getClassFinalGrades(
            @PathVariable Long courseClassId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(gradeService.getClassFinalGrades(courseClassId, current, size));
    }

    @PostMapping("/submit/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> submitGrades(@PathVariable Long courseClassId) {
        gradeService.submitGrades(courseClassId);
        return Result.success();
    }

    @PostMapping("/publish/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> publishGrades(@PathVariable Long courseClassId) {
        gradeService.publishGrades(courseClassId);
        return Result.success();
    }

    @GetMapping("/published/{courseClassId}")
    public Result<Boolean> isGradesPublished(@PathVariable Long courseClassId) {
        return Result.success(gradeService.isGradesPublished(courseClassId));
    }

    @GetMapping("/stats/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Map<String, Object>> getGradeStatistics(@PathVariable Long courseClassId) {
        return Result.success(gradeService.getGradeStatistics(courseClassId));
    }

    @GetMapping("/search/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<List<FinalGrade>> searchStudentGrades(
            @PathVariable Long courseClassId,
            @RequestParam String studentNumber) {
        return Result.success(gradeService.searchStudentGrades(courseClassId, studentNumber));
    }
}
