package com.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.CourseClass;
import com.student.service.CourseClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course-class")
public class CourseClassController {

    @Autowired
    private CourseClassService courseClassService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseClass> createCourseClass(@RequestBody CourseClass courseClass) {
        courseClassService.createCourseClass(courseClass);
        return Result.success(courseClass);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<CourseClass> updateCourseClass(@RequestBody CourseClass courseClass) {
        courseClassService.updateCourseClass(courseClass);
        return Result.success(courseClass);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCourseClass(@PathVariable Long id) {
        courseClassService.deleteCourseClass(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CourseClass> getCourseClass(@PathVariable Long id) {
        CourseClass courseClass = courseClassService.getCourseClassById(id);
        return Result.success(courseClass);
    }

    @GetMapping
    public Result<Page<CourseClass>> listCourseClasses(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<CourseClass> pageResult = courseClassService.listCourseClasses(courseId, teacherId, semester, status, page, size);
        return Result.success(pageResult);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishCourseClass(@PathVariable Long id) {
        courseClassService.publishCourseClass(id);
        return Result.success();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> closeCourseClass(@PathVariable Long id) {
        courseClassService.closeCourseClass(id);
        return Result.success();
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and #teacherId == authentication.principal.id)")
    public Result<Page<CourseClass>> listTeacherCourseClasses(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<CourseClass> pageResult = courseClassService.listTeacherCourseClasses(teacherId, semester, status, page, size);
        return Result.success(pageResult);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #studentId == authentication.principal.id)")
    public Result<Page<CourseClass>> listStudentCourseClasses(
            @PathVariable Long studentId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<CourseClass> pageResult = courseClassService.listStudentCourseClasses(studentId, semester, status, page, size);
        return Result.success(pageResult);
    }
}
