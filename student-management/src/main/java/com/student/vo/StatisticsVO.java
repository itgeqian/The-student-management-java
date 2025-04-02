package com.student.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    
    @Data
    public static class CourseStatistics {
        private Long totalCourses;          // 总课程数
        private Long activeCourses;         // 活跃课程数
        private Double averageScore;        // 平均分
        private Double passRate;            // 及格率
        private Map<String, Long> gradeDistribution;  // 成绩分布
        private List<Map<String, Object>> scoresTrend; // 成绩趋势
    }
    
    @Data
    public static class HomeworkStatistics {
        private Long totalHomeworks;        // 总作业数
        private Long submittedCount;        // 已提交数
        private Long pendingGradeCount;     // 待批改数
        private Double averageScore;        // 平均分
        private Double submitRate;          // 提交率
        private Map<String, Long> scoreDistribution;  // 分数分布
        private List<Map<String, Object>> submissionTrend; // 提交趋势
    }
    
    @Data
    public static class StudentStatistics {
        private Long totalStudents;         // 总学生数
        private Long activeStudents;        // 活跃学生数
        private Double averageScore;        // 平均分
        private Double homeworkSubmitRate;  // 作业提交率
        private Map<String, Double> performanceMetrics; // 表现指标
        private List<Map<String, Object>> activityTrend; // 活动趋势
    }
    
    private CourseStatistics courseStats;
    private HomeworkStatistics homeworkStats;
    private StudentStatistics studentStats;
    private String timeRange;               // 统计时间范围
    private Map<String, Object> customMetrics; // 自定义指标
}
