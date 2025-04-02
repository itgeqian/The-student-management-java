package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.CourseSelection;

import java.util.List;
import java.util.Map;

public interface CourseSelectionService extends IService<CourseSelection> {
    
    /**
     * 学生选课
     */
    CourseSelection selectCourse(Long studentId, Long courseClassId);
    
    /**
     * 退课
     */
    void dropCourse(Long studentId, Long courseClassId);
    
    /**
     * 获取学生的选课记录
     */
    IPage<CourseSelection> getStudentSelections(Long studentId, Page<CourseSelection> page);
    
    /**
     * 获取课程班级的选课记录
     * @param courseClassId 课程班级ID
     * @param courseName 课程名称（可选）
     * @param page 分页参数
     * @return 选课记录分页结果
     */
    IPage<CourseSelection> getCourseClassSelections(Long courseClassId, String courseName, Page<CourseSelection> page);
    
    /**
     * 统计课程班级的选课人数
     */
    Long countByCourseClass(Long courseClassId);
    
    /**
     * 检查学生是否已选择某课程
     */
    boolean hasSelected(Long studentId, Long courseClassId);
    
    /**
     * 检查课程是否已满
     */
    boolean isCourseFull(Long courseClassId);
    
    /**
     * 获取课程统计信息
     */
    Map<String, Object> getCourseStatistics(Long courseId);
    
    /**
     * 获取学生的平均分
     */
    double getStudentAverageScore(Long studentId);
    
    /**
     * 获取学生选课的课程班级ID列表
     */
    List<Long> getStudentCourseClassIds(Long studentId);
}
