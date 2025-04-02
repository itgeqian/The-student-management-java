package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Course;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;

public interface CourseService extends IService<Course> {
    Course createCourse(Course course);
    Course updateCourse(Course course);
    void deleteCourse(Long id);
    Course getCourseById(Long id);
    /**
     * 获取课程列表
     * @param keyword 关键字（课程名称）
     * @param semester 学期
     * @param departmentId 部门ID
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<Course> getCourses(String keyword, String semester, Long departmentId, Page<Course> page);
    List<Course> getTeacherCourses(Long teacherId);

    // 统计方法
    long count(LambdaQueryWrapper<Course> wrapper);
    
    long countActiveCourses(LambdaQueryWrapper<Course> wrapper);
}
