package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Course;
import com.student.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Course> createCourse(@RequestBody Course course) {
        return Result.success(courseService.createCourse(course));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Course> updateCourse(@RequestBody Course course) {
        return Result.success(courseService.updateCourse(course));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Course> getCourse(@PathVariable Long id) {
        return Result.success(courseService.getCourseById(id));
    }

    @GetMapping
    public Result<IPage<Course>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Course> page = new Page<>(current, size);
        return Result.success(courseService.getCourses(keyword, semester, departmentId, page));
    }
}
