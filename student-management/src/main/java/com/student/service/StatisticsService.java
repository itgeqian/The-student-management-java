package com.student.service;

import com.student.vo.StatisticsVO;
import java.time.LocalDateTime;
import java.util.Map;

public interface StatisticsService {
    
    /**
     * 获取课程统计数据
     */
    StatisticsVO.CourseStatistics getCourseStatistics(Long courseId, Long teacherId, 
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取作业统计数据
     */
    StatisticsVO.HomeworkStatistics getHomeworkStatistics(Long courseId, Long teacherId, 
            Long studentId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取学生统计数据
     */
    StatisticsVO.StudentStatistics getStudentStatistics(Long courseId, Long studentId, 
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取综合统计数据
     */
    StatisticsVO getOverallStatistics(Long userId, String role, 
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取趋势数据
     */
    Map<String, Object> getTrendData(String type, String metric, Long userId, String role, 
            LocalDateTime startTime, LocalDateTime endTime);
}
