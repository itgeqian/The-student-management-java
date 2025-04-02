package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.Student;
import com.student.entity.Teacher;
import com.student.entity.User;
import com.student.mapper.UserMapper;
import com.student.service.StudentService;
import com.student.service.TeacherService;
import com.student.service.UserService;
import com.student.utils.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    @Lazy
    private StudentService studentService;

    @Autowired
    @Lazy
    private TeacherService teacherService;
    
    private User findByUsername(String username) {
        return lambdaQuery()
            .eq(User::getUsername, username)
            .one();
    }

    @Override
    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (existsByUsername(user.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 保存用户
        save(user);
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        // 检查用户是否存在
        if (!lambdaQuery().eq(User::getId, user.getId()).exists()) {
            throw new BusinessException("User not found");
        }

        // 如果要更新密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        removeById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery()
            .eq(User::getUsername, username)
            .exists();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }
    

    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("Username cannot be empty");
        }
        
        User user = lambdaQuery()
            .eq(User::getUsername, username)
            .one();
            
        if (user == null) {
            throw new BusinessException("User not found");
        }

        // 根据角色查询额外信息
        if ("STUDENT".equals(user.getRole())) {
            Student student = studentService.getById(user.getId());
            user.setStudentInfo(student);
        } else if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherService.getById(user.getId());
            user.setTeacherInfo(teacher);
        }
        
        return user;
    }

    @Override
    public IPage<User> getUsers(String username, Page<User> page) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("Username cannot be empty");
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("Password cannot be empty");
        }
        
        User user = getUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }
        
        // TODO: 这里应该生成JWT token
        return "token";
    }
}
