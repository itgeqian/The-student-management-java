package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.dto.GradeRequest;
import com.student.dto.HomeworkWithGradeDTO;
import com.student.entity.FinalGrade;
import com.student.entity.GradeComposition;
import com.student.entity.Homework;
import com.student.entity.HomeworkSubmission;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HomeworkService extends IService<Homework> {
    // 作业管理
    Homework createHomework(Homework homework);
    Homework updateHomework(Homework homework);
    void deleteHomework(Long id);
    Homework getHomeworkById(Long id);
    /**
     * 获取教师布置的作业列表
     * @param teacherId 教师ID
     * @param keyword 关键字（标题或内容）
     * @param type 作业类型
     * @param courseId 课程ID
     * @param page 分页参数
     * @return 作业分页列表（包含提交统计）
     */
    IPage<Homework> getHomeworksByTeacher(Long teacherId, String keyword, String type, Long courseId, Page<Homework> page);
    IPage<Homework> getHomeworksByCourseClass(Long courseClassId, Page<Homework> page);

    // 作业提交
    HomeworkSubmission submitHomework(HomeworkSubmission submission);
    HomeworkSubmission updateSubmission(HomeworkSubmission submission);
    HomeworkSubmission getSubmissionById(Long id);
    IPage<HomeworkSubmission> getSubmissionsByStudent(Long studentId, Page<HomeworkSubmission> page);
    IPage<HomeworkSubmission> getSubmissionsByHomework(Long homeworkId, Page<HomeworkSubmission> page);
    IPage<HomeworkSubmission> getSubmissionsByStudentAndCourseClass(Long studentId, Long courseClassId, Page<HomeworkSubmission> page);

    // 作业评分
    HomeworkSubmission gradeHomework(Long submissionId, Integer score, String comment);
    void returnHomework(Long submissionId, String comment);
    Double calculateStudentHomeworkAverage(Long studentId, Long courseClassId);

    /**
     * 获取学生可提交的作业列表
     * 包括：
     * 1. 学生已选课程班级的作业
     * 2. 作业截止日期未到
     * 3. 学生尚未提交或作业被退回需要重新提交的作业
     */
    IPage<Homework> getStudentAvailableHomeworks(Long studentId, Page<Homework> page);

    /**
     * 批量评分作业
     * @param gradeRequests 评分请求列表
     */
    void batchGradeHomework(List<GradeRequest> gradeRequests);

    // 获取作业和成绩列表
    IPage<HomeworkWithGradeDTO> getHomeworksWithGrade(Long homeworkId);

    // 获取课程班级的所有学生总成绩
    IPage<FinalGrade> getTotalScore(Long courseClassId, Page<FinalGrade> page);

    // 获取已发布的总成绩
    IPage<FinalGrade> getPublishedScores(Long courseClassId, Page<FinalGrade> page);

    // 获取学生所有作业成绩
    IPage<HomeworkSubmission> getStudentAllHomeworkGrades(Long studentId, Page<HomeworkSubmission> page);
    
    Map<String, Long> getSubmissionStats(Long courseId, Long teacherId, Long studentId);
    
    Double getAverageScore(Long courseId, Long teacherId, Long studentId);
    
    Map<String, Long> getScoreDistribution(Long courseId, Long teacherId, Long studentId);
    
    List<Map<String, Object>> getSubmissionTrend(Long courseId, Long teacherId, Long studentId, 
            LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取教师名下课程班级的成绩构成
     * 如果只有一条数据，直接返回该成绩构成
     * 如果有多条数据但成绩构成不一致，返回全0
     * 如果有多条数据且成绩构成一致，返回该成绩构成
     * @param teacherId 教师ID
     * @return 成绩构成
     */
    GradeComposition getTeacherGradeComposition(Long teacherId);

    /**
     * 获取教师名下的作业提交记录
     * 
     * @param teacherId 教师ID
     * @param studentName 学生姓名（模糊查询）
     * @param studentNo 学生学号（模糊查询）
     * @param courseName 课程名称（模糊查询）
     * @param courseCode 课程编码（模糊查询）
     * @param page 分页参数
     * @return 分页的作业提交记录
     */
    IPage<HomeworkSubmission> getTeacherGrades(Long teacherId, String homeworkType, String studentName, String studentNo, 
            String courseName, String courseCode, Page<HomeworkSubmission> page);

    /**
     * 更新教师名下所有课程班级的成绩构成
     * @param teacherId 教师ID
     * @param gradeComposition 成绩构成
     */
    void updateTeacherGradeComposition(Long teacherId, GradeComposition gradeComposition);
}
