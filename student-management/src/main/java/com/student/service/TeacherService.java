package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Teacher;
import java.util.List;

public interface TeacherService extends IService<Teacher> {
    Teacher createTeacher(Teacher teacher);
    List<Teacher> createTeachers(List<Teacher> teachers);
    Teacher updateTeacher(Teacher teacher);
    void deleteTeacher(Long id);
    void deleteTeachers(List<Long> ids);
    Teacher getTeacherById(Long id);
    Teacher getTeacherByNumber(String teacherNumber);
    IPage<Teacher> getTeachers(String name, String departmentId, Page<Teacher> page);
    IPage<Teacher> getTeachersByDepartment(Long departmentId, Page<Teacher> page);
    boolean existsByTeacherNumber(String teacherNumber);
    void assignCourse(Long teacherId, Long courseId);
    void removeCourse(Long teacherId, Long courseId);
    void importTeachers(String filePath);
}
