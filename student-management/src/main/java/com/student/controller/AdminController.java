package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Course;
import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.entity.User;
import com.student.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private CourseService courseService;

    // 用户管理
    @PostMapping("/users")
    public Result<User> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping("/users")
    public Result<User> updateUser(@RequestBody User user) {
        return Result.success(userService.updateUser(user));
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/users/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/users")
    public Result<IPage<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<User> page = new Page<>(current, size);
        return Result.success(userService.getUsers(username, page));
    }

    // 教师管理
    @PostMapping("/teachers")
    public Result<Teacher> createTeacher(@RequestBody Teacher teacher) {
        return Result.success(teacherService.createTeacher(teacher));
    }

    @PostMapping("/teachers/batch")
    public Result<List<Teacher>> createTeachers(@RequestBody List<Teacher> teachers) {
        return Result.success(teacherService.createTeachers(teachers));
    }

    @PutMapping("/teachers")
    public Result<Teacher> updateTeacher(@RequestBody Teacher teacher) {
        return Result.success(teacherService.updateTeacher(teacher));
    }

    @DeleteMapping("/teachers/{id}")
    public Result<?> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return Result.success();
    }

    @DeleteMapping("/teachers")
    public Result<?> deleteTeachers(@RequestBody List<Long> ids) {
        teacherService.deleteTeachers(ids);
        return Result.success();
    }

    @GetMapping("/teachers/{id}")
    public Result<Teacher> getTeacher(@PathVariable Long id) {
        return Result.success(teacherService.getTeacherById(id));
    }

    @GetMapping("/teachers")
    public Result<IPage<Teacher>> getTeachers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String departmentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Teacher> page = new Page<>(current, size);
        return Result.success(teacherService.getTeachers(name, departmentId, page));
    }

    @PostMapping("/teachers/import")
    public Result<?> importTeachers(@RequestParam String filePath) {
        teacherService.importTeachers(filePath);
        return Result.success();
    }

    @PostMapping("/teachers/{teacherId}/courses/{courseId}")
    public Result<?> assignCourse(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {
        teacherService.assignCourse(teacherId, courseId);
        return Result.success();
    }

    @DeleteMapping("/teachers/{teacherId}/courses/{courseId}")
    public Result<?> removeCourse(
            @PathVariable Long teacherId,
            @PathVariable Long courseId) {
        teacherService.removeCourse(teacherId, courseId);
        return Result.success();
    }

    // 学生管理
    @PostMapping("/students")
    public Result<Student> createStudent(@RequestBody Student student) {
        return Result.success(studentService.createStudent(student));
    }

    @PostMapping("/students/batch")
    public Result<List<Student>> createStudents(@RequestBody List<Student> students) {
        return Result.success(studentService.createStudents(students));
    }

    @PutMapping("/students")
    public Result<Student> updateStudent(@RequestBody Student student) {
        return Result.success(studentService.updateStudent(student));
    }

    @DeleteMapping("/students/{id}")
    public Result<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return Result.success();
    }

    @DeleteMapping("/students")
    public Result<?> deleteStudents(@RequestBody List<Long> ids) {
        studentService.deleteStudents(ids);
        return Result.success();
    }

    @GetMapping("/students/{id}")
    public Result<Student> getStudent(@PathVariable Long id) {
        return Result.success(studentService.getStudentById(id));
    }

    @GetMapping("/students")
    public Result<IPage<Student>> getStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Student> page = new Page<>(current, size);
        return Result.success(studentService.getStudents(keyword, departmentId, page));
    }

    @PostMapping("/students/import")
    public Result<?> importStudents(@RequestParam String filePath) {
        studentService.importStudents(filePath);
        return Result.success();
    }

    @PostMapping("/students/export")
    public Result<?> exportStudents(@RequestParam String filePath) {
        studentService.exportStudents(filePath);
        return Result.success();
    }
}
