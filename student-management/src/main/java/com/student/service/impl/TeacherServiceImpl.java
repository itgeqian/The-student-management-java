package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.common.PageRequest;
import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.entity.User;
import com.student.entity.Course;
import com.student.mapper.TeacherMapper;
import com.student.service.TeacherService;
import com.student.service.UserService;
import com.student.service.CourseService;
import com.student.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Autowired
    private UserService userService;
    
    @Autowired
    @Lazy
    private CourseService courseService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional
    public Teacher createTeacher(Teacher teacher) {
        validateTeacher(teacher);

        if (existsByStudentNo(teacher.getTeacherNo())) {
            throw new BusinessException("Teacher number already exists");
        }

        teacher.setId(snowflakeIdGenerator.nextId());
        teacher.setUserId(teacher.getId());
        save(teacher);
        // 创建用户账号
        User user = new User();
        user.setId(teacher.getId());
        user.setUsername(teacher.getTeacherNo());
        user.setPassword("123456"); // 默认密码
        user.setRole("TEACHER");
        user.setRealName(teacher.getName());
        userService.createUser(user);
        
        return teacher;
    }

    @Override
    @Transactional
    public Teacher updateTeacher(Teacher teacher) {
        if (!lambdaQuery().eq(Teacher::getId, teacher.getId()).exists()) {
            throw new BusinessException("Teacher not found");
        }
        validateTeacher(teacher);
        updateById(teacher);
        return teacher;
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = getById(id);
        if (teacher == null) {
            throw new BusinessException("Teacher not found");
        }
        
        // 检查是否有关联的课程
        List<Course> teacherCourses = courseService.getTeacherCourses(id);
        if (teacherCourses != null && !teacherCourses.isEmpty()) {
            throw new BusinessException("Cannot delete teacher with active courses");
        }
        
        // 删除关联的用户账号
        if (teacher.getUserId() != null) {
            userService.deleteUser(teacher.getUserId());
        }
        
        removeById(id);
    }

    @Override
    public Teacher getTeacherById(Long id) {
        Teacher teacher = getById(id);
        if (teacher != null && teacher.getUserId() != null) {
            teacher.setUser(userService.getUserById(teacher.getUserId()));
        }
        return teacher;
    }

    @Override
    public Teacher getTeacherByNumber(String teacherNumber) {
        if (!StringUtils.hasText(teacherNumber)) {
            throw new BusinessException("Teacher number cannot be empty");
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getName, teacherNumber);
        return getOne(wrapper);
    }

    @Override
    public IPage<Teacher> getTeachers(String name, String departmentId, Page<Teacher> page) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Teacher::getName, name).or().like(Teacher::getTeacherNo, name);
        }
        if (departmentId != null) {
            wrapper.eq(Teacher::getDepartmentId, departmentId);
        }
        wrapper.orderByAsc(Teacher::getName);
        return page(page, wrapper);
    }

    @Override
    public IPage<Teacher> getTeachersByDepartment(Long departmentId, Page<Teacher> page) {
        if (departmentId == null) {
            throw new BusinessException("Department ID cannot be null");
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getDepartmentId, departmentId)
               .orderByAsc(Teacher::getName);
        return page(page, wrapper);
    }

    @Override
    public boolean existsByTeacherNumber(String teacherNumber) {
        if (!StringUtils.hasText(teacherNumber)) {
            return false;
        }
        
        return lambdaQuery()
            .eq(Teacher::getName, teacherNumber)
            .exists();
    }

    @Override
    @Transactional
    public List<Teacher> createTeachers(List<Teacher> teachers) {
        teachers.forEach(this::createTeacher);
        return teachers;
    }

    @Override
    @Transactional
    public void deleteTeachers(List<Long> ids) {
        removeByIds(ids);
    }

    @Override
    @Transactional
    public void assignCourse(Long teacherId, Long courseId) {
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            throw new RuntimeException("Teacher not found");
        }
        // TODO: Implement course assignment logic
        // This will depend on your course-teacher relationship model
    }

    @Override
    @Transactional
    public void removeCourse(Long teacherId, Long courseId) {
        Teacher teacher = getById(teacherId);
        if (teacher == null) {
            throw new RuntimeException("Teacher not found");
        }
        // TODO: Implement course removal logic
        // This will depend on your course-teacher relationship model
    }

    @Override
    @Transactional
    public void importTeachers(String filePath) {
        // TODO: Implement file import logic
        // This should read the file and create teachers from the data
        throw new UnsupportedOperationException("Teacher import not implemented yet");
    }

    public boolean existsByStudentNo(String teacherNo) {
        if (!StringUtils.hasText(teacherNo)) {
            return false;
        }

        return lambdaQuery()
                .eq(Teacher::getTeacherNo, teacherNo)
                .exists();
    }

    private void validateTeacher(Teacher teacher) {
        if (!StringUtils.hasText(teacher.getName())) {
            throw new BusinessException("Teacher name cannot be empty");
        }
        if (teacher.getDepartmentId() == null) {
            throw new BusinessException("Department ID cannot be empty");
        }
    }
}
