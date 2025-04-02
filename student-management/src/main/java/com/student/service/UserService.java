package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends IService<User> {
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    User getUserByUsername(String username);
    IPage<User> getUsers(String username, Page<User> page);
    boolean existsByUsername(String username);
    String login(String username, String password);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
