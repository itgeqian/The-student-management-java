package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.common.PageRequest;
import com.student.entity.Student;
import com.student.entity.User;
import com.student.mapper.StudentMapper;
import com.student.service.StudentService;
import com.student.service.UserService;
import com.student.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional
    public Student createStudent(Student student) {
        validateStudent(student);
        
        // 检查学号是否已存在
        if (existsByStudentNo(student.getStudentNo())) {
            throw new BusinessException("Student number already exists");
        }
        
        // 检查手机号是否已存在
        if (existsByPhone(student.getPhone())) {
            throw new BusinessException("Phone number already exists");
        }
        
        // 检查邮箱是否已存在
        if (existsByEmail(student.getEmail())) {
            throw new BusinessException("Email already exists");
        }
        student.setId(snowflakeIdGenerator.nextId());
        student.setUserId(student.getId());
        save(student);
        
        // 创建用户账号
        User user = new User();
        user.setId(student.getId());
        user.setUsername(student.getStudentNo());
        user.setPassword("123456"); // 默认密码
        user.setRole("STUDENT");
        user.setRealName(student.getName());
        User createdUser = userService.createUser(user);
        
        return student;
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        validateStudent(student);
        
        Student existingStudent = getById(student.getId());
        if (existingStudent == null) {
            throw new BusinessException("Student not found");
        }
        
        // 检查学号是否重复
        if (!existingStudent.getStudentNo().equals(student.getStudentNo())) {
            if (existsByStudentNo(student.getStudentNo())) {
                throw new BusinessException("Student number already exists");
            }
        }
        
        // 检查手机号是否重复
        if (!Objects.equals(existingStudent.getPhone(), student.getPhone())) {
            if (existsByPhone(student.getPhone())) {
                throw new BusinessException("Phone number already exists");
            }
        }
        
        // 检查邮箱是否重复
        if (!Objects.equals(existingStudent.getEmail(), student.getEmail())) {
            if (existsByEmail(student.getEmail())) {
                throw new BusinessException("Email already exists");
            }
        }
        
        updateById(student);
        return student;
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getById(id);
        if (student == null) {
            throw new BusinessException("Student not found");
        }
        
        // 删除关联的用户账号
        if (student.getUserId() != null) {
            userService.deleteUser(student.getUserId());
        }
        
        removeById(id);
    }

    @Override
    public Student getStudentById(Long id) {
        Student student = getById(id);
        if (student != null && student.getUserId() != null) {
            student.setUser(userService.getUserById(student.getUserId()));
        }
        return student;
    }
    
    public boolean existsByStudentNo(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return false;
        }
        
        return lambdaQuery()
            .eq(Student::getStudentNo, studentNo)
            .exists();
    }
    
    public boolean existsByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        
        return lambdaQuery()
            .eq(Student::getPhone, phone)
            .exists();
    }
    
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        
        return lambdaQuery()
            .eq(Student::getEmail, email)
            .exists();
    }

    @Override
    public Student getStudentByNumber(String studentNumber) {
        if (!StringUtils.hasText(studentNumber)) {
            throw new BusinessException("Student number cannot be empty");
        }
        
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, studentNumber);
        return getOne(wrapper);
    }

    @Override
    public IPage<Student> getStudents(String name, Page<Student> page) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Student::getName, name);
        }
        wrapper.orderByAsc(Student::getStudentNo);
        return page(page, wrapper);
    }

    @Override
    public IPage<Student> getStudentsByDepartment(Long departmentId, Page<Student> page) {
        if (departmentId == null) {
            throw new BusinessException("Department ID cannot be null");
        }
        
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getDepartmentId, departmentId)
               .orderByAsc(Student::getStudentNo);
        return page(page, wrapper);
    }

    @Override
    public List<Student> getStudentsByCourseClass(Long courseClassId) {
        if (courseClassId == null) {
            throw new BusinessException("Course class ID cannot be null");
        }
        
        return lambdaQuery()
            .inSql(Student::getId, 
                "SELECT student_id FROM course_class_student WHERE course_class_id = " + courseClassId)
            .orderByAsc(Student::getStudentNo)
            .list();
    }

    @Override
    public boolean existsByStudentNumber(String studentNumber) {
        if (!StringUtils.hasText(studentNumber)) {
            return false;
        }
        
        return lambdaQuery()
            .eq(Student::getStudentNo, studentNumber)
            .exists();
    }

    private void validateStudent(Student student) {
        if (!StringUtils.hasText(student.getStudentNo())) {
            throw new BusinessException("Student number cannot be empty");
        }
        if (!StringUtils.hasText(student.getName())) {
            throw new BusinessException("Student name cannot be empty");
        }
        if (student.getDepartmentId() == null) {
            throw new BusinessException("Department ID cannot be empty");
        }
        if (!StringUtils.hasText(student.getGrade())) {
            throw new BusinessException("Grade cannot be empty");
        }
        if (!StringUtils.hasText(student.getClassName())) {
            throw new BusinessException("Class name cannot be empty");
        }
    }

    @Override
    @Transactional
    public List<Student> createStudents(List<Student> students) {
        for (Student student : students) {
            validateStudent(student);
            save(student);
        }
        return students;
    }

    @Override
    @Transactional
    public void deleteStudents(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }

    @Override
    @Transactional
    public void importStudents(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new BusinessException("File path cannot be empty");
        }
        // TODO: Implement file import logic using CSV or Excel reader
        throw new UnsupportedOperationException("Student import not implemented yet");
    }

    @Override
    @Transactional
    public void exportStudents(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new BusinessException("File path cannot be empty");
        }
        // TODO: Implement file export logic using CSV or Excel writer
        throw new UnsupportedOperationException("Student export not implemented yet");
    }

    @Override
    public IPage<Student> getStudents(String keyword, Long departmentId, Page<Student> page) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 添加姓名关键字查询
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Student::getName, keyword)
                  .or()
                  .like(Student::getStudentNo, keyword);
        }
        
        // 添加部门查询
        if (departmentId != null) {
            wrapper.eq(Student::getDepartmentId, departmentId);
        }
        
        // 按学号排序
        wrapper.orderByAsc(Student::getStudentNo);
        
        return page(page, wrapper);
    }
}
