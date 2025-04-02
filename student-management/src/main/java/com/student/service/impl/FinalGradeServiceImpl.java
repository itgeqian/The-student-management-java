package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.FinalGrade;
import com.student.mapper.FinalGradeMapper;
import com.student.service.FinalGradeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinalGradeServiceImpl extends ServiceImpl<FinalGradeMapper, FinalGrade> implements FinalGradeService {

    @Override
    public Map<String, Long> getGradeDistribution(Long courseId, Long teacherId) {
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(FinalGrade::getCourseClassId, courseId);
        }
        if (teacherId != null) {
            wrapper.inSql(FinalGrade::getCourseClassId,
                    "SELECT id FROM course_class WHERE teacher_id = " + teacherId);
        }

        wrapper.isNotNull(FinalGrade::getTotalScore);

        List<FinalGrade> grades = list(wrapper);
        Map<String, Long> distribution = new HashMap<>();

        grades.forEach(grade -> {
            String level = getGradeLevel(grade.getTotalScore());
            distribution.merge(level, 1L, Long::sum);
        });

        return distribution;
    }

    @Override
    public Double getAverageScore(Long courseId, Long teacherId) {
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(FinalGrade::getCourseClassId, courseId);
        }
        if (teacherId != null) {
            wrapper.inSql(FinalGrade::getCourseClassId,
                    "SELECT id FROM course_class WHERE teacher_id = " + teacherId);
        }

        wrapper.isNotNull(FinalGrade::getTotalScore);

        List<FinalGrade> grades = list(wrapper);
        if (grades.isEmpty()) {
            return 0.0;
        }

        double sum = grades.stream()
                .mapToInt(FinalGrade::getTotalScore)
                .sum();
        return sum / grades.size();
    }

    @Override
    public List<Map<String, Object>> getScoresTrend(Long courseId, Long teacherId,
            LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(FinalGrade::getCourseClassId, courseId);
        }
        if (teacherId != null) {
            wrapper.inSql(FinalGrade::getCourseClassId,
                    "SELECT id FROM course_class WHERE teacher_id = " + teacherId);
        }

        if (startTime != null) {
            wrapper.ge(FinalGrade::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(FinalGrade::getCreateTime, endTime);
        }

        wrapper.orderByAsc(FinalGrade::getCreateTime);

        List<FinalGrade> grades = list(wrapper);
        List<Map<String, Object>> trend = new ArrayList<>();
        // 按天统计平均分
        Map<LocalDate, DoubleSummaryStatistics> dailyStats = grades.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getCreateTime().toLocalDate(),
                        Collectors.summarizingDouble(FinalGrade::getTotalScore)
                ));

        dailyStats.forEach((date, stats) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", date);
            point.put("average", stats.getAverage());
            trend.add(point);
        });

        return trend;
    }

    @Override
    public Double getStudentAverageScore(Long courseId, Long studentId) {
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();

        if (courseId != null) {
            wrapper.eq(FinalGrade::getCourseClassId, courseId);
        }
        if (studentId != null) {
            wrapper.eq(FinalGrade::getStudentId, studentId);
        }

        wrapper.isNotNull(FinalGrade::getTotalScore);

        List<FinalGrade> grades = list(wrapper);
        if (grades.isEmpty()) {
            return 0.0;
        }

        double sum = grades.stream()
                .mapToInt(FinalGrade::getTotalScore)
                .sum();
        return sum / grades.size();
    }

    private String getGradeLevel(Integer score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
