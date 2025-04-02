package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.Course;
import com.student.entity.CourseClass;
import com.student.entity.CourseSelection;
import com.student.mapper.CourseMapper;
import com.student.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    @Lazy
    private TeacherService teacherService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    @Lazy
    private CourseClassService courseClassService;

    @Autowired
    @Lazy
    private CourseSelectionService courseSelectionService;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        validateCourse(course);
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        course.setIsActive(true); // 默认激活
        save(course);
        return course;
    }

    @Override
    @Transactional
    public Course updateCourse(Course course) {
        validateCourse(course);
        Course existingCourse = getById(course.getId());
        if (existingCourse == null) {
            throw new BusinessException("Course not found");
        }
        
        course.setUpdateTime(LocalDateTime.now());
        updateById(course);
        return course;
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("Course not found");
        }
        
        removeById(id);
    }

    @Override
    public Course getCourseById(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException("Course not found");
        }
        
        // 填充关联数据
        if (course.getTeacherId() != null) {
            course.setTeacher(teacherService.getById(course.getTeacherId()));
        }
        if (course.getDepartmentId() != null) {
            course.setDepartment(departmentService.getById(course.getDepartmentId()));
        }
        
        return course;
    }

    @Override
    public IPage<Course> getCourses(String keyword, String semester, Long departmentId, Page<Course> page) {
        LambdaQueryWrapper<Course> wrapper = Wrappers.lambdaQuery();
        
        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Course::getName, keyword)
                .or()
                .like(Course::getCourseCode, keyword)
                .or()
                .like(Course::getDescription, keyword)
                .or()
                .like(Course::getSemester, keyword)
                .or()
                .like(Course::getClassroom, keyword)
            );
        }
        // 添加学期查询
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Course::getSemester, semester);
        }

        // 添加部门查询
        if (departmentId != null) {
            wrapper.eq(Course::getDepartmentId, departmentId);
        }
        
        // 默认只显示激活的课程
        wrapper.eq(Course::getIsActive, true);
        wrapper.orderByDesc(Course::getCreateTime);
        
        // 执行分页查询
        IPage<Course> result = page(page, wrapper);
        
        // 填充关联数据
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Course course : result.getRecords()) {
                if (course.getTeacherId() != null) {
                    course.setTeacher(teacherService.getById(course.getTeacherId()));
                }
                if (course.getDepartmentId() != null) {
                    course.setDepartment(departmentService.getById(course.getDepartmentId()));
                }
                
                // 计算学生总数
                // 先获取该课程的所有班级
                List<CourseClass> courseClasses = courseClassService.lambdaQuery()
                    .eq(CourseClass::getCourseId, course.getId())
                    .list();
                
                if (!courseClasses.isEmpty()) {
                    // 获取这些班级的选课记录总数
                    List<Long> courseClassIds = courseClasses.stream()
                        .map(CourseClass::getId)
                        .collect(Collectors.toList());

                    
                    long count = courseSelectionService.lambdaQuery()
                        .in(CourseSelection::getCourseClassId, courseClassIds)
                        .eq(CourseSelection::getStatus, "SELECTED")  // 只统计未退课的学生
                        .count();
                        
                    course.setStudentCount(count);
                } else {
                    course.setStudentCount(0L);
                }
            }
        }
        
        return result;
    }

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        // 验证教师是否存在
        if (teacherService.getById(teacherId) == null) {
            throw new BusinessException("Teacher not found");
        }
        
        LambdaQueryWrapper<Course> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Course::getTeacherId, teacherId)
               .eq(Course::getIsActive, true)
               .orderByDesc(Course::getCreateTime);
                    
        List<Course> courses = list(wrapper);
        
        // 填充关联数据
        for (Course course : courses) {
            if (course.getTeacherId() != null) {
                course.setTeacher(teacherService.getById(course.getTeacherId()));
            }
            if (course.getDepartmentId() != null) {
                course.setDepartment(departmentService.getById(course.getDepartmentId()));
            }
        }
        
        return courses;
    }
    
    @Override
    public long count(LambdaQueryWrapper<Course> wrapper) {
        return this.baseMapper.selectCount(wrapper);
    }
    
    @Override
    public long countActiveCourses(LambdaQueryWrapper<Course> wrapper) {
        wrapper.eq(Course::getIsActive, true);
        return count(wrapper);
    }

    private void validateCourse(Course course) {
        if (!StringUtils.hasText(course.getName())) {
            throw new BusinessException("Course name cannot be empty");
        }
        if (!StringUtils.hasText(course.getCourseCode())) {
            throw new BusinessException("Course code cannot be empty");
        }
        if (course.getDepartmentId() == null) {
            throw new BusinessException("Department ID cannot be null");
        }
        if (course.getCredit() == null || course.getCredit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Credit must be greater than 0");
        }
        if (!StringUtils.hasText(course.getSemester())) {
            throw new BusinessException("Semester cannot be empty");
        }
        
        // 检查课程代码是否重复
        LambdaQueryWrapper<Course> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Course::getCourseCode, course.getCourseCode());
        if (course.getId() != null) {
            wrapper.ne(Course::getId, course.getId());
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("Course code already exists");
        }
    }
}
