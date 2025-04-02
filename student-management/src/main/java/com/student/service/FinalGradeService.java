package com.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.FinalGrade;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface FinalGradeService extends IService<FinalGrade> {

    // 统计方法
    Map<String, Long> getGradeDistribution(Long courseId, Long teacherId);
    
    Double getAverageScore(Long courseId, Long teacherId);
    
    List<Map<String, Object>> getScoresTrend(Long courseId, Long teacherId, 
            LocalDateTime startTime, LocalDateTime endTime);
    
    Double getStudentAverageScore(Long courseId, Long studentId);

}
