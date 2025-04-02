package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.dto.GradeRequest;
import com.student.dto.HomeworkWithGradeDTO;
import com.student.entity.FinalGrade;
import com.student.entity.GradeComposition;
import com.student.entity.Homework;
import com.student.entity.HomeworkSubmission;
import com.student.service.HomeworkService;
import com.student.service.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private HomeworkSubmissionService homeworkSubmissionService;

    // 教师操作
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Homework> createHomework(@RequestBody Homework homework) {
        return Result.success(homeworkService.createHomework(homework));
    }

    @PutMapping
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Homework> updateHomework(@RequestBody Homework homework) {
        return Result.success(homeworkService.updateHomework(homework));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> deleteHomework(@PathVariable Long id) {
        homeworkService.deleteHomework(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Homework> getHomework(@PathVariable Long id) {
        return Result.success(homeworkService.getHomeworkById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<IPage<Homework>> getTeacherHomeworks(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Homework> page = new Page<>(current, size);
        return Result.success(homeworkService.getHomeworksByTeacher(teacherId, keyword, type, courseId, page));
    }

    @GetMapping("/class/{courseClassId}")
    public Result<IPage<Homework>> getCourseClassHomeworks(
            @PathVariable Long courseClassId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Homework> page = new Page<>(current, size);
        return Result.success(homeworkService.getHomeworksByCourseClass(courseClassId, page));
    }

    // 学生操作
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<HomeworkSubmission> submitHomework(@RequestBody HomeworkSubmission submission) {
        return Result.success(homeworkService.submitHomework(submission));
    }

    @PutMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<HomeworkSubmission> updateSubmission(@RequestBody HomeworkSubmission submission) {
        return Result.success(homeworkService.updateSubmission(submission));
    }

    @GetMapping("/submission/{id}")
    public Result<HomeworkSubmission> getSubmission(@PathVariable Long id) {
        return Result.success(homeworkService.getSubmissionById(id));
    }

    @GetMapping("/submission/student/{studentId}")
    public Result<IPage<HomeworkSubmission>> getStudentSubmissions(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<HomeworkSubmission> page = new Page<>(current, size);
        return Result.success(homeworkService.getSubmissionsByStudent(studentId, page));
    }

    @GetMapping("/submission/homework/{homeworkId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<IPage<HomeworkSubmission>> getHomeworkSubmissions(
            @PathVariable Long homeworkId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<HomeworkSubmission> page = new Page<>(current, size);
        return Result.success(homeworkService.getSubmissionsByHomework(homeworkId, page));
    }

    // 获取学生可提交的作业列表
    @GetMapping("/student/{studentId}/available")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id")
    public Result<IPage<Homework>> getStudentAvailableHomeworks(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Homework> page = new Page<>(current, size);
        IPage<Homework> homeworks = homeworkService.getStudentAvailableHomeworks(studentId, page);
        return Result.success(homeworks);
    }

    // 作业评分
    @PostMapping("/grade/{submissionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<HomeworkSubmission> gradeHomework(
            @PathVariable Long submissionId,
            @RequestBody GradeRequest gradeRequest) {
        return Result.success(homeworkService.gradeHomework(
            submissionId, 
            gradeRequest.getScore(), 
            gradeRequest.getComment()));
    }

    @DeleteMapping("/submission/{id}")
    public Result<Boolean> deleteSubmission(@PathVariable Long id) {
        return Result.success(homeworkSubmissionService.removeById(id));
    }

    @PostMapping("/return/{submissionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> returnHomework(
            @PathVariable Long submissionId,
            @RequestParam String comment) {
        homeworkService.returnHomework(submissionId, comment);
        return Result.success();
    }

    // 批量作业评分
    @PostMapping("/grade/batch")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<?> batchGradeHomework(@RequestBody List<GradeRequest> gradeRequests) {
        homeworkService.batchGradeHomework(gradeRequests);
        return Result.success();
    }

    // 统计
    @GetMapping("/stats/student/{studentId}/class/{courseClassId}")
    public Result<Double> getStudentHomeworkAverage(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        return Result.success(homeworkService.calculateStudentHomeworkAverage(studentId, courseClassId));
    }

    @GetMapping("/submission/student/{studentId}/class/{courseClassId}")
    public Result<IPage<HomeworkSubmission>> getStudentClassSubmissions(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<HomeworkSubmission> page = new Page<>(current, size);
        return Result.success(homeworkService.getSubmissionsByStudentAndCourseClass(
                studentId, courseClassId, page));
    }

    @GetMapping("/withgrade")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<IPage<HomeworkWithGradeDTO>> getHomeworksWithGrade(
            @RequestParam Long homeworkId) {
        return Result.success(homeworkService.getHomeworksWithGrade(homeworkId));
    }

    @GetMapping("/score/total")
    public Result<IPage<FinalGrade>> getTotalScore(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FinalGrade> page = new Page<>(current, size);
        return Result.success(homeworkService.getTotalScore(userId, page));
    }

    @GetMapping("/score/published/{courseClassId}")
    public Result<IPage<FinalGrade>> getPublishedScores(
            @PathVariable Long courseClassId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FinalGrade> page = new Page<>(current, size);
        return Result.success(homeworkService.getPublishedScores(courseClassId, page));
    }

    @GetMapping("/grades/student/{studentId}")
    public Result<IPage<HomeworkSubmission>> getStudentAllHomeworkGrades(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<HomeworkSubmission> page = new Page<>(current, size);
        return Result.success(homeworkService.getStudentAllHomeworkGrades(studentId, page));
    }

    @GetMapping("/teacher/{teacherId}/grades")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and #teacherId == authentication.principal.id)")
    public Result<IPage<HomeworkSubmission>> getTeacherGrades(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String homeworkType,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseCode,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<HomeworkSubmission> page = new Page<>(current, size);
        return Result.success(homeworkService.getTeacherGrades(teacherId, homeworkType, studentName, studentNo, courseName, courseCode, page));
    }

    /**
     * 获取教师名下课程班级的成绩构成
     * 如果只有一条数据，直接返回该成绩构成
     * 如果有多条数据但成绩构成不一致，返回全0
     * 如果有多条数据且成绩构成一致，返回该成绩构成
     */
    @GetMapping("/teacher/{teacherId}/grade-composition")
    @PreAuthorize("hasRole('TEACHER') and #teacherId == authentication.principal.id")
    public Result<GradeComposition> getTeacherGradeComposition(@PathVariable Long teacherId) {
        return Result.success(homeworkService.getTeacherGradeComposition(teacherId));
    }

    /**
     * 更新教师名下所有课程班级的成绩构成
     */
    @PutMapping("/teacher/{teacherId}/grade-composition")
    @PreAuthorize("hasRole('TEACHER') and #teacherId == authentication.principal.id")
    public Result<Void> updateTeacherGradeComposition(
            @PathVariable Long teacherId,
            @RequestBody GradeComposition gradeComposition) {
        homeworkService.updateTeacherGradeComposition(teacherId, gradeComposition);
        return Result.success();
    }
}
