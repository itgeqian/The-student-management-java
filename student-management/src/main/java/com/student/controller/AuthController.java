package com.student.controller;

import com.student.common.Result;
import com.student.entity.User;
import com.student.service.UserService;
import com.student.security.JwtTokenUtil;
import com.student.dto.LoginResponse;
import com.student.dto.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }
        return Result.success(userService.createUser(user));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenUtil.generateToken(authentication);
            
            User user = userService.getUserByUsername(loginRequest.getUsername());
            user.setPassword(null); // 清除密码
            
            LoginResponse response = new LoginResponse(jwt, user);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Error: " + e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/change-password")
    public Result<?> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        userService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> resetPassword(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 重置密码为123456
        user.setPassword(passwordEncoder.encode("123456"));
        boolean success = userService.updateById(user);

        if (success) {
            return Result.success(true);
        } else {
            return Result.error("密码重置失败");
        }
    }
}
