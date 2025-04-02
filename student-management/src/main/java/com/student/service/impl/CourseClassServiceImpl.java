package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.*;
import com.student.mapper.CourseClassMapper;
import com.student.service.CourseClassService;
import com.student.service.CourseService;
import com.student.service.TeacherService;
import com.student.service.CourseSelectionService;
import com.student.service.HomeworkSubmissionService;
import com.student.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseClassServiceImpl extends ServiceImpl<CourseClassMapper, CourseClass> implements CourseClassService {

    @Autowired
    @Lazy
    private CourseService courseService;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    @Lazy
    private CourseSelectionService courseSelectionService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private HomeworkSubmissionService homeworkSubmissionService;

    @Override
    @Transactional
    public void createCourseClass(CourseClass courseClass) {
        validateCourseClass(courseClass);
        courseClass.setIsActive(false); // 默认为未激活状态
        courseClass.setId(snowflakeIdGenerator.nextId());
        save(courseClass);
    }

    @Override
    @Transactional
    public void updateCourseClass(CourseClass courseClass) {
        validateCourseClass(courseClass);
        
        // 检查班级是否存在
        CourseClass existingClass = getById(courseClass.getId());
        if (existingClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        // 已激活的班级不能修改某些信息
        if (existingClass.getIsActive()) {
            if (!existingClass.getCourseId().equals(courseClass.getCourseId()) ||
                !existingClass.getTeacherId().equals(courseClass.getTeacherId()) ||
                !existingClass.getSemester().equals(courseClass.getSemester())) {
                throw new BusinessException("Cannot modify basic information of active course class");
            }
        }
        
        // 检查班级容量是否小于已选课人数
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseClassId, courseClass.getId())
               .eq(CourseSelection::getStatus, "SELECTED");
        long currentStudents = courseSelectionService.count(wrapper);
        
        if (courseClass.getMaxStudent() < currentStudents) {
            throw new BusinessException("New capacity cannot be less than current number of students: " + currentStudents);
        }
        
        updateById(courseClass);
    }

    @Override
    @Transactional
    public void deleteCourseClass(Long id) {
        CourseClass courseClass = getById(id);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        // 已激活的班级不能删除
        if (courseClass.getIsActive()) {
            throw new BusinessException("Cannot delete active course class");
        }
        
        // 检查是否有学生选课
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getCourseClassId, id)
               .eq(CourseSelection::getStatus, "SELECTED");
        long count = courseSelectionService.count(wrapper);
        if (count > 0) {
            throw new BusinessException("Cannot delete course class with enrolled students");
        }
        
        removeById(id);
    }

    @Override
    public CourseClass getCourseClassById(Long id) {
        CourseClass courseClass = getById(id);
        if (courseClass != null) {
            fillAssociatedData(courseClass);
        }
        return courseClass;
    }

    @Override
    public Page<CourseClass> listCourseClasses(Long courseId, Long teacherId, String semester, String status, long page, long size) {
        LambdaQueryWrapper<CourseClass> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(CourseClass::getCourseId, courseId);
        }
        if (teacherId != null) {
            wrapper.eq(CourseClass::getTeacherId, teacherId);
        }
        if (StringUtils.hasText(semester)) {
            wrapper.eq(CourseClass::getSemester, semester);
        }
        if (StringUtils.hasText(status)) {
            // 根据状态参数转换为isActive条件
            if ("PUBLISHED".equals(status) || "ACTIVE".equals(status)) {
                wrapper.eq(CourseClass::getIsActive, true);
            } else if ("DRAFT".equals(status) || "INACTIVE".equals(status)) {
                wrapper.eq(CourseClass::getIsActive, false);
            }
        }
        wrapper.orderByDesc(CourseClass::getCreateTime);
        
        Page<CourseClass> pageParam = new Page<>(page, size);
        Page<CourseClass> result = page(pageParam, wrapper);
        
        // 批量填充关联数据
        fillAssociatedData(result.getRecords());
        
        return result;
    }

    @Override
    @Transactional
    public void publishCourseClass(Long id) {
        CourseClass courseClass = getById(id);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        if (courseClass.getIsActive()) {
            throw new BusinessException("Course class is already active");
        }
        
        courseClass.setIsActive(true);
        updateById(courseClass);
    }

    @Override
    @Transactional
    public void closeCourseClass(Long id) {
        CourseClass courseClass = getById(id);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        if (!courseClass.getIsActive()) {
            throw new BusinessException("Course class is already inactive");
        }
        
        courseClass.setIsActive(false);
        updateById(courseClass);
    }

    @Override
    public Page<CourseClass> listTeacherCourseClasses(Long teacherId, String semester, String status, long page, long size) {
        return listCourseClasses(null, teacherId, semester, status, page, size);
    }

    @Override
    public Page<CourseClass> listStudentCourseClasses(Long studentId, String semester, String status, long page, long size) {
        // 获取学生选课的班级ID列表
        LambdaQueryWrapper<CourseSelection> selectionWrapper = new LambdaQueryWrapper<>();
        selectionWrapper.eq(CourseSelection::getStudentId, studentId)
                       .eq(CourseSelection::getStatus, "SELECTED");
        List<CourseSelection> selections = courseSelectionService.list(selectionWrapper);
        List<Long> classIds = selections.stream()
                                      .map(CourseSelection::getCourseClassId)
                                      .collect(Collectors.toList());
        
        if (classIds.isEmpty()) {
            return new Page<>(page, size);
        }
        
        // 查询班级信息
        LambdaQueryWrapper<CourseClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseClass::getId, classIds);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(CourseClass::getSemester, semester);
        }
        if (StringUtils.hasText(status)) {
            // 根据状态参数转换为isActive条件
            if ("PUBLISHED".equals(status) || "ACTIVE".equals(status)) {
                wrapper.eq(CourseClass::getIsActive, true);
            } else if ("DRAFT".equals(status) || "INACTIVE".equals(status)) {
                wrapper.eq(CourseClass::getIsActive, false);
            }
        }
        wrapper.orderByDesc(CourseClass::getCreateTime);
        
        Page<CourseClass> pageParam = new Page<>(page, size);
        Page<CourseClass> result = page(pageParam, wrapper);
        
        // 批量填充关联数据
        fillAssociatedData(result.getRecords());
        
        return result;
    }
    
    private void validateCourseClass(CourseClass courseClass) {
        // 检查课程是否存在
        if (!courseService.lambdaQuery().eq(Course::getId, courseClass.getCourseId()).exists()) {
            throw new BusinessException("Course not found");
        }
        
        // 检查教师是否存在
        if (!teacherService.lambdaQuery().eq(Teacher::getId, courseClass.getTeacherId()).exists()) {
            throw new BusinessException("Teacher not found");
        }
        
        // 检查班级容量是否合理
        if (courseClass.getMaxStudent() == null || courseClass.getMaxStudent() <= 0) {
            throw new BusinessException("Invalid class capacity");
        }
        
        // 检查学期是否为空
        if (!StringUtils.hasText(courseClass.getSemester())) {
            throw new BusinessException("Semester cannot be empty");
        }
    }
    
    private void fillAssociatedData(List<CourseClass> courseClasses) {
        if (courseClasses == null || courseClasses.isEmpty()) {
            return;
        }

        // 获取所有需要查询的ID
        List<Long> courseIds = courseClasses.stream()
            .map(CourseClass::getCourseId)
            .distinct()
            .collect(Collectors.toList());
        
        List<Long> teacherIds = courseClasses.stream()
            .map(CourseClass::getTeacherId)
            .distinct()
            .collect(Collectors.toList());

        // 批量查询课程和教师信息
        Map<Long, Course> courseMap = courseService.listByIds(courseIds)
            .stream()
            .collect(Collectors.toMap(Course::getId, course -> course));
            
        Map<Long, Teacher> teacherMap = teacherService.listByIds(teacherIds)
            .stream()
            .collect(Collectors.toMap(Teacher::getId, teacher -> teacher));

        // 填充关联数据
        courseClasses.forEach(courseClass -> {
            courseClass.setCourse(courseMap.get(courseClass.getCourseId()));
            courseClass.setTeacher(teacherMap.get(courseClass.getTeacherId()));
        });
    }

    private void fillAssociatedData(CourseClass courseClass) {
        if (courseClass == null) {
            return;
        }
        fillAssociatedData(Collections.singletonList(courseClass));
    }
}
