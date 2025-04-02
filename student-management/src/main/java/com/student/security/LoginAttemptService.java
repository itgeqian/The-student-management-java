package com.student.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPT = 5;  // 最大尝试次数
    private static final long BLOCK_DURATION = 3600000;  // 锁定时长（1小时）

    // 用户名 -> 失败次数的映射
    private final Map<String, Integer> attemptMap = new ConcurrentHashMap<>();
    // 用户名 -> 锁定时间的映射
    private final Map<String, Long> blockMap = new ConcurrentHashMap<>();

    /**
     * 登录成功，清除记录
     */
    public void loginSucceeded(String username) {
        attemptMap.remove(username);
        blockMap.remove(username);
    }

    /**
     * 登录失败，增加失败次数
     */
    public void loginFailed(String username) {
        int attempts = attemptMap.getOrDefault(username, 0) + 1;
        attemptMap.put(username, attempts);
        
        // 如果达到最大尝试次数，记录锁定时间
        if (attempts >= MAX_ATTEMPT) {
            blockMap.put(username, System.currentTimeMillis());
        }
    }

    /**
     * 检查用户是否被锁定
     */
    public boolean isBlocked(String username) {
        // 如果没有锁定记录，直接返回false
        Long blockTime = blockMap.get(username);
        if (blockTime == null) {
            return false;
        }
        
        // 检查是否已经过了锁定时间
        if (System.currentTimeMillis() - blockTime >= BLOCK_DURATION) {
            // 锁定时间已过，清除记录
            attemptMap.remove(username);
            blockMap.remove(username);
            return false;
        }
        
        return true;
    }
}
