package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.*;
import com.student.mapper.CourseSelectionMapper;
import com.student.service.CourseClassService;
import com.student.service.CourseSelectionService;
import com.student.service.CourseService;
import com.student.service.StudentService;
import com.student.service.TeacherService;
import com.student.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {

    @Autowired
    private StudentService studentService;
    
    @Autowired
    @Lazy
    private CourseClassService courseClassService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    @Lazy
    private CourseService courseService;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    @Transactional
    public CourseSelection selectCourse(Long studentId, Long courseClassId) {
        // 检查学生是否存在
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new BusinessException("Student not found");
        }

        // 检查课程班级是否存在
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }

        // 检查是否已经选过这门课
        if (hasSelected(studentId, courseClassId)) {
            throw new BusinessException("You have already selected this course");
        }

        // 检查课程是否已满
        if (isCourseFull(courseClassId)) {
            throw new BusinessException("Course class is full");
        }

        // 创建选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseClassId(courseClassId);
        selection.setStatus("SELECTED");
        selection.setSelectTime(LocalDateTime.now());
        selection.setUpdateTime(LocalDateTime.now());

        // 初始化成绩为0
        selection.setHomeworkScore(0);
        selection.setExperimentScore(0);
        selection.setFinalExamScore(0);
        selection.setTotalScore(0);

        save(selection);
        
        // 填充关联数据
        selection.setStudent(student);
        selection.setCourseClass(courseClass);
        
        return selection;
    }

    @Override
    @Transactional
    public void dropCourse(Long studentId, Long courseClassId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
               .eq(CourseSelection::getCourseClassId, courseClassId)
               .eq(CourseSelection::getStatus, "SELECTED"); // 只能退选已选课程
        
        CourseSelection selection = getOne(wrapper);
        if (selection == null) {
            throw new BusinessException("Course selection not found");
        }
        
        // 检查是否可以退课（例如：课程开始后不能退课）
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass != null && !courseClass.getIsActive()) {
            throw new BusinessException("Cannot drop course after it has started");
        }
        
        // 更新状态为已退课
        selection.setStatus("DROPPED");
        selection.setUpdateTime(LocalDateTime.now());
        updateById(selection);
    }

    @Override
    public IPage<CourseSelection> getStudentSelections(Long studentId, Page<CourseSelection> page) {
        // 验证学生是否存在
        if (!studentService.lambdaQuery().eq(Student::getId, studentId).exists()) {
            throw new BusinessException("Student not found");
        }
        
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
               .orderByDesc(CourseSelection::getSelectTime);
               
        IPage<CourseSelection> result = page(page, wrapper);
        
        // 填充关联数据
        result.getRecords().forEach(selection -> {
            selection.setStudent(studentService.getById(selection.getStudentId()));
            selection.setCourseClass(courseClassService.getCourseClassById(selection.getCourseClassId()));
        });
        
        return result;
    }

    @Override
    public IPage<CourseSelection> getCourseClassSelections(Long id, String courseName, Page<CourseSelection> page) {
        String userType = commonUtils.getUserType(id);

        // 1. 创建查询条件
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        if ("teacher".equalsIgnoreCase(userType)) {
            // 查询教师所有的课程班级id
            List<Long> courseClassIds = courseClassService.lambdaQuery()
                    .eq(CourseClass::getTeacherId, id)
                    .list()
                    .stream()
                    .map(CourseClass::getId)
                    .collect(Collectors.toList());
            wrapper.in(CourseSelection::getCourseClassId, courseClassIds);
        }

        // 如果提供了课程名称，需要先找到匹配的课程班级
        if (StringUtils.hasText(courseName)) {
            // 查找包含指定课程名称的课程
            List<Course> courses = courseService.lambdaQuery()
                    .like(Course::getName, courseName)
                    .list();
            
            if (!courses.isEmpty()) {
                // 获取这些课程对应的课程班级ID列表
                List<Long> courseIds = courses.stream()
                        .map(Course::getId)
                        .collect(Collectors.toList());
                
                List<CourseClass> courseClasses = courseClassService.lambdaQuery()
                        .in(CourseClass::getCourseId, courseIds)
                        .list();
                
                List<Long> courseClassIds = courseClasses.stream()
                        .map(CourseClass::getId)
                        .collect(Collectors.toList());
                
                // 添加课程班级ID条件
                wrapper.in(CourseSelection::getCourseClassId, courseClassIds);
            } else {
                // 如果没有找到匹配的课程，返回空结果
                return new Page<>();
            }
        }

        // 2. 执行分页查询
        IPage<CourseSelection> selectionPage = page(page, wrapper);

        // 3. 填充每个选课记录的详细信息
        selectionPage.getRecords().forEach(selection -> {
            // 获取课程班级信息，包括教师和课程详情
            CourseClass courseClass = courseClassService.getCourseClassById(selection.getCourseClassId());
            if (courseClass != null) {
                // 获取并设置教师信息
                Teacher teacher = teacherService.getById(courseClass.getTeacherId());
                courseClass.setTeacher(teacher);
                
                // 获取并设置课程信息
                Course course = courseService.getById(courseClass.getCourseId());
                courseClass.setCourse(course);
            }
            selection.setCourseClass(courseClass);

            // 获取学生信息
            Student student = studentService.getById(selection.getStudentId());
            selection.setStudent(student);
        });

        return selectionPage;
    }

    @Override
    public Long countByCourseClass(Long courseClassId) {
        return lambdaQuery()
            .eq(CourseSelection::getCourseClassId, courseClassId)
            .eq(CourseSelection::getStatus, "SELECTED") // 只统计选课状态的记录
            .count();
    }

    @Override
    public boolean hasSelected(Long studentId, Long courseClassId) {
        return lambdaQuery()
            .eq(CourseSelection::getStudentId, studentId)
            .eq(CourseSelection::getCourseClassId, courseClassId)
            .eq(CourseSelection::getStatus, "SELECTED") // 只检查选课状态的记录
            .exists();
    }

    @Override
    public boolean isCourseFull(Long courseClassId) {
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        long currentCount = countByCourseClass(courseClassId);
        return currentCount >= courseClass.getMaxStudent();
    }

    @Override
    public Map<String, Object> getCourseStatistics(Long courseId) {
        // 获取该课程下所有的班级
        Page<CourseClass> coursePage = courseClassService.listCourseClasses(courseId, null, null, null, 1, Integer.MAX_VALUE);
        List<Long> courseClassIds = coursePage.getRecords().stream()
                                            .map(CourseClass::getId)
                                            .collect(Collectors.toList());
        
        if (courseClassIds.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalStudents", 0L);
            Map<String, Double> emptyScores = new HashMap<>();
            emptyScores.put("homework", 0.0);
            emptyScores.put("experiment", 0.0);
            emptyScores.put("finalExam", 0.0);
            emptyScores.put("total", 0.0);
            emptyStats.put("averageScores", emptyScores);
            return emptyStats;
        }
        
        // 统计选课人数
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(CourseSelection::getCourseClassId, courseClassIds)
               .eq(CourseSelection::getStatus, "SELECTED");
        long totalStudents = count(wrapper);
        
        // 统计平均分
        Map<String, Double> averageScores = new HashMap<>();
        
        // 作业平均分
        double avgHomework = lambdaQuery()
            .in(CourseSelection::getCourseClassId, courseClassIds)
            .eq(CourseSelection::getStatus, "COMPLETED")
            .list()
            .stream()
            .mapToInt(CourseSelection::getHomeworkScore)
            .average()
            .orElse(0.0);
        averageScores.put("homework", avgHomework);
        
        // 实验平均分
        double avgExperiment = lambdaQuery()
            .in(CourseSelection::getCourseClassId, courseClassIds)
            .eq(CourseSelection::getStatus, "COMPLETED")
            .list()
            .stream()
            .mapToInt(CourseSelection::getExperimentScore)
            .average()
            .orElse(0.0);
        averageScores.put("experiment", avgExperiment);
        
        // 期末考试平均分
        double avgFinal = lambdaQuery()
            .in(CourseSelection::getCourseClassId, courseClassIds)
            .eq(CourseSelection::getStatus, "COMPLETED")
            .list()
            .stream()
            .mapToInt(CourseSelection::getFinalExamScore)
            .average()
            .orElse(0.0);
        averageScores.put("finalExam", avgFinal);
        
        // 总分平均分
        double avgTotal = lambdaQuery()
            .in(CourseSelection::getCourseClassId, courseClassIds)
            .eq(CourseSelection::getStatus, "COMPLETED")
            .list()
            .stream()
            .mapToInt(CourseSelection::getTotalScore)
            .average()
            .orElse(0.0);
        averageScores.put("total", avgTotal);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalStudents", totalStudents);
        statistics.put("averageScores", averageScores);
        return statistics;
    }

    @Override
    public double getStudentAverageScore(Long studentId) {
        // 验证学生是否存在
        if (!studentService.lambdaQuery().eq(Student::getId, studentId).exists()) {
            throw new BusinessException("Student not found");
        }
        
        // 获取学生所有已完成课程的总分平均值
        return lambdaQuery()
            .eq(CourseSelection::getStudentId, studentId)
            .eq(CourseSelection::getStatus, "COMPLETED")
            .list()
            .stream()
            .mapToInt(CourseSelection::getTotalScore)
            .average()
            .orElse(0.0);
    }

    @Override
    public List<Long> getStudentCourseClassIds(Long studentId) {
        return this.list(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, "SELECTED"))
                .stream()
                .map(CourseSelection::getCourseClassId)
                .collect(Collectors.toList());
    }
}
