package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.CourseSelection;
import com.student.service.CourseSelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course-selection")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService courseSelectionService;

    @PostMapping("/{studentId}/{courseClassId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<CourseSelection> selectCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        return Result.success(courseSelectionService.selectCourse(studentId, courseClassId));
    }

    @DeleteMapping("/{studentId}/{courseClassId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<?> dropCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        courseSelectionService.dropCourse(studentId, courseClassId);
        return Result.success();
    }

    @GetMapping("/student/{studentId}")
    public Result<IPage<CourseSelection>> getStudentSelections(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<CourseSelection> page = new Page<>(current, size);
        return Result.success(courseSelectionService.getStudentSelections(studentId, page));
    }

    @GetMapping("/class/{courseClassId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<IPage<CourseSelection>> getCourseClassSelections(
            @PathVariable Long courseClassId,
            @RequestParam(required = false) String courseName,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<CourseSelection> page = new Page<>(current, size);
        return Result.success(courseSelectionService.getCourseClassSelections(courseClassId, courseName, page));
    }

    @GetMapping("/count/{courseClassId}")
    public Result<Long> countByCourseClass(@PathVariable Long courseClassId) {
        return Result.success(courseSelectionService.countByCourseClass(courseClassId));
    }

    @GetMapping("/check-full/{courseClassId}")
    public Result<Boolean> isCourseFull(@PathVariable Long courseClassId) {
        return Result.success(courseSelectionService.isCourseFull(courseClassId));
    }

    @GetMapping("/check-selected/{studentId}/{courseClassId}")
    public Result<Boolean> hasSelected(
            @PathVariable Long studentId,
            @PathVariable Long courseClassId) {
        return Result.success(courseSelectionService.hasSelected(studentId, courseClassId));
    }
}
