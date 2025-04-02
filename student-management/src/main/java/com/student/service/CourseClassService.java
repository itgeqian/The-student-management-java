package com.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.CourseClass;

public interface CourseClassService extends IService<CourseClass> {

    /**
     * 创建课程班级
     */
    void createCourseClass(CourseClass courseClass);

    /**
     * 更新课程班级
     */
    void updateCourseClass(CourseClass courseClass);

    /**
     * 删除课程班级
     */
    void deleteCourseClass(Long id);

    /**
     * 获取课程班级
     */
    CourseClass getCourseClassById(Long id);

    /**
     * 获取课程班级列表
     */
    Page<CourseClass> listCourseClasses(Long courseId, Long teacherId, String semester, String status, long page, long size);

    /**
     * 发布课程班级
     */
    void publishCourseClass(Long id);

    /**
     * 关闭课程班级
     */
    void closeCourseClass(Long id);

    /**
     * 获取教师的课程班级列表
     */
    Page<CourseClass> listTeacherCourseClasses(Long teacherId, String semester, String status, long page, long size);

    /**
     * 获取学生的课程班级列表
     */
    Page<CourseClass> listStudentCourseClasses(Long studentId, String semester, String status, long page, long size);
}
