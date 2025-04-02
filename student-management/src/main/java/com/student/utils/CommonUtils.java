package com.student.utils;

import com.student.entity.User;
import com.student.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {
    @Autowired
    private UserService userService;

    public String getUserType(Long id) {
        User user = userService.getUserById(id);
        return user.getRole();
    }
}
