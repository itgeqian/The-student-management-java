//package com.student.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.student.entity.*;
//import com.student.mapper.*;
//import com.student.service.*;
//import com.student.vo.StatisticsVO;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class StatisticsServiceImpl implements StatisticsService {
//
//    private final CourseService courseService;
//    private final HomeworkService homeworkService;
//    private final HomeworkSubmissionService homeworkSubmissionService;
//    private final StudentService studentService;
//    private final FinalGradeService finalGradeService;
//
//    @Override
//    public StatisticsVO.CourseStatistics getCourseStatistics(Long courseId, Long teacherId, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        StatisticsVO.CourseStatistics stats = new StatisticsVO.CourseStatistics();
//        
//        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
//        if (courseId != null) {
//            wrapper.eq(Course::getId, courseId);
//        }
//        if (teacherId != null) {
//            wrapper.eq(Course::getTeacherId, teacherId);
//        }
//        if (startTime != null) {
//            wrapper.ge(Course::getCreateTime, startTime);
//        }
//        if (endTime != null) {
//            wrapper.le(Course::getCreateTime, endTime);
//        }
//
//        // 计算课程统计数据
//        stats.setTotalCourses(courseService.count(wrapper));
//        stats.setActiveCourses(courseService.countActiveCourses(wrapper));
//        
//        // 计算成绩分布
//        Map<String, Long> gradeDistribution = finalGradeService.getGradeDistribution(courseId, teacherId);
//        stats.setGradeDistribution(gradeDistribution);
//        
//        // 计算平均分和及格率
//        Double avgScore = finalGradeService.getAverageScore(courseId, teacherId);
//        stats.setAverageScore(avgScore);
//        stats.setPassRate(calculatePassRate(gradeDistribution));
//        
//        // 计算成绩趋势
//        List<Map<String, Object>> scoresTrend = finalGradeService.getScoresTrend(courseId, teacherId, startTime, endTime);
//        stats.setScoresTrend(scoresTrend);
//        
//        return stats;
//    }
//
//    @Override
//    public StatisticsVO.HomeworkStatistics getHomeworkStatistics(Long courseId, Long teacherId, 
//            Long studentId, LocalDateTime startTime, LocalDateTime endTime) {
//        StatisticsVO.HomeworkStatistics stats = new StatisticsVO.HomeworkStatistics();
//        
//        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
//        if (courseId != null) {
//            wrapper.eq(Homework::getCourseClassId, courseId);
//        }
//        if (startTime != null) {
//            wrapper.ge(Homework::getCreateTime, startTime);
//        }
//        if (endTime != null) {
//            wrapper.le(Homework::getCreateTime, endTime);
//        }
//
//        // 计算作业统计数据
//        stats.setTotalHomeworks(homeworkService.count(wrapper));
//        
//        // 计算提交统计
//        Map<String, Long> submissionStats = homeworkSubmissionService.getSubmissionStats(courseId, teacherId, studentId);
//        stats.setSubmittedCount(submissionStats.getOrDefault("submitted", 0L));
//        stats.setPendingGradeCount(submissionStats.getOrDefault("pending", 0L));
//        
//        // 计算平均分和提交率
//        Double avgScore = homeworkSubmissionService.getAverageScore(courseId, teacherId, studentId);
//        stats.setAverageScore(avgScore);
//        stats.setSubmitRate(calculateSubmitRate(submissionStats));
//        
//        // 计算分数分布
//        Map<String, Long> scoreDistribution = homeworkSubmissionService.getScoreDistribution(courseId, teacherId, studentId);
//        stats.setScoreDistribution(scoreDistribution);
//        
//        // 计算提交趋势
//        List<Map<String, Object>> submissionTrend = homeworkSubmissionService.getSubmissionTrend(courseId, teacherId, studentId, startTime, endTime);
//        stats.setSubmissionTrend(submissionTrend);
//        
//        return stats;
//    }
//
//    @Override
//    public StatisticsVO.StudentStatistics getStudentStatistics(Long courseId, Long studentId, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        StatisticsVO.StudentStatistics stats = new StatisticsVO.StudentStatistics();
//        
//        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
//        if (courseId != null) {
//            // 通过课程ID筛选学生
//            wrapper.inSql(Student::getId, 
//                "SELECT student_id FROM course_student WHERE course_id = " + courseId);
//        }
//        if (studentId != null) {
//            wrapper.eq(Student::getId, studentId);
//        }
//
//        // 计算学生统计数据
//        stats.setTotalStudents(studentService.count(wrapper));
//        stats.setActiveStudents(studentService.countActiveStudents(wrapper));
//        
//        // 计算平均分
//        Double avgScore = finalGradeService.getStudentAverageScore(courseId, studentId);
//        stats.setAverageScore(avgScore);
//        
//        // 计算作业提交率
//        Double submitRate = homeworkSubmissionService.getStudentSubmitRate(courseId, studentId);
//        stats.setHomeworkSubmitRate(submitRate);
//        
//        // 计算表现指标
//        Map<String, Double> performanceMetrics = calculatePerformanceMetrics(courseId, studentId);
//        stats.setPerformanceMetrics(performanceMetrics);
//        
//        // 计算活动趋势
//        List<Map<String, Object>> activityTrend = getStudentActivityTrend(courseId, studentId, startTime, endTime);
//        stats.setActivityTrend(activityTrend);
//        
//        return stats;
//    }
//
//    @Override
//    public StatisticsVO getOverallStatistics(Long userId, String role, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        StatisticsVO stats = new StatisticsVO();
//        
//        // 根据角色和用户ID获取相应的统计数据
//        if ("ADMIN".equals(role)) {
//            stats.setCourseStats(getCourseStatistics(null, null, startTime, endTime));
//            stats.setHomeworkStats(getHomeworkStatistics(null, null, null, startTime, endTime));
//            stats.setStudentStats(getStudentStatistics(null, null, startTime, endTime));
//        } else if ("TEACHER".equals(role)) {
//            stats.setCourseStats(getCourseStatistics(null, userId, startTime, endTime));
//            stats.setHomeworkStats(getHomeworkStatistics(null, userId, null, startTime, endTime));
//            stats.setStudentStats(getStudentStatistics(null, null, startTime, endTime));
//        } else if ("STUDENT".equals(role)) {
//            stats.setHomeworkStats(getHomeworkStatistics(null, null, userId, startTime, endTime));
//            stats.setStudentStats(getStudentStatistics(null, userId, startTime, endTime));
//        }
//        
//        stats.setTimeRange(startTime + " to " + endTime);
//        stats.setCustomMetrics(calculateCustomMetrics(userId, role));
//        
//        return stats;
//    }
//
//    @Override
//    public Map<String, Object> getTrendData(String type, String metric, Long userId, String role, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        Map<String, Object> result = new HashMap<>();
//        List<Map<String, Object>> trendData = new ArrayList<>();
//        
//        switch (type.toLowerCase()) {
//            case "course":
//                trendData = getCourseMetricTrend(metric, userId, role, startTime, endTime);
//                break;
//            case "homework":
//                trendData = getHomeworkMetricTrend(metric, userId, role, startTime, endTime);
//                break;
//            case "student":
//                trendData = getStudentMetricTrend(metric, userId, role, startTime, endTime);
//                break;
//        }
//        
//        result.put("type", type);
//        result.put("metric", metric);
//        result.put("data", trendData);
//        return result;
//    }
//
//    // 辅助方法
//    private Double calculatePassRate(Map<String, Long> gradeDistribution) {
//        long total = gradeDistribution.values().stream().mapToLong(Long::longValue).sum();
//        if (total == 0) return 0.0;
//        
//        long passed = gradeDistribution.entrySet().stream()
//                .filter(e -> !e.getKey().equals("F"))
//                .mapToLong(Map.Entry::getValue)
//                .sum();
//        
//        return (double) passed / total * 100;
//    }
//
//    private Double calculateSubmitRate(Map<String, Long> submissionStats) {
//        long total = submissionStats.values().stream().mapToLong(Long::longValue).sum();
//        if (total == 0) return 0.0;
//        
//        long submitted = submissionStats.getOrDefault("submitted", 0L);
//        return (double) submitted / total * 100;
//    }
//
//    private Map<String, Double> calculatePerformanceMetrics(Long courseId, Long studentId) {
//        Map<String, Double> metrics = new HashMap<>();
//        
//        // 计算各项指标
//        metrics.put("homeworkCompletionRate", homeworkSubmissionService.getStudentSubmitRate(courseId, studentId));
//        metrics.put("averageScore", finalGradeService.getStudentAverageScore(courseId, studentId));
//        metrics.put("participationRate", calculateParticipationRate(courseId, studentId));
//        
//        return metrics;
//    }
//
//    private Double calculateParticipationRate(Long courseId, Long studentId) {
//        // 实现参与度计算逻辑
//        return 0.0; // 待实现
//    }
//
//    private List<Map<String, Object>> getStudentActivityTrend(Long courseId, Long studentId, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        // 实现学生活动趋势统计逻辑
//        return new ArrayList<>(); // 待实现
//    }
//
//    private Map<String, Object> calculateCustomMetrics(Long userId, String role) {
//        Map<String, Object> metrics = new HashMap<>();
//        // 根据角色计算自定义指标
//        return metrics;
//    }
//
//    private List<Map<String, Object>> getCourseMetricTrend(String metric, Long userId, String role, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        // 实现课程指标趋势统计逻辑
//        return new ArrayList<>(); // 待实现
//    }
//
//    private List<Map<String, Object>> getHomeworkMetricTrend(String metric, Long userId, String role, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        // 实现作业指标趋势统计逻辑
//        return new ArrayList<>(); // 待实现
//    }
//
//    private List<Map<String, Object>> getStudentMetricTrend(String metric, Long userId, String role, 
//            LocalDateTime startTime, LocalDateTime endTime) {
//        // 实现学生指标趋势统计逻辑
//        return new ArrayList<>(); // 待实现
//    }
//}
