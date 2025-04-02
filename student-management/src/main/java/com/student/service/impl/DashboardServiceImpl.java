//package com.student.service.impl;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.student.entity.*;
//import com.student.mapper.*;
//import com.student.service.DashboardService;
//import com.student.vo.DashboardStatsVO;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class DashboardServiceImpl implements DashboardService {
//
//    private final CourseMapper courseMapper;
//    private final StudentMapper studentMapper;
//    private final HomeworkMapper homeworkMapper;
//    private final HomeworkSubmissionMapper submissionMapper;
//    private final FinalGradeMapper gradeMapper;
//    private final CourseSelectionMapper selectionMapper;
//
//    @Override
//    public DashboardStatsVO getStats(String semester) {
//        // 如果未指定学期，使用当前学期
//        if (!StringUtils.hasText(semester)) {
//            semester = getCurrentSemester();
//        }
//
//        DashboardStatsVO stats = new DashboardStatsVO();
//        String userRole = SecurityUtils.getCurrentUserRole();
//        Long userId = SecurityUtils.getCurrentUserId();
//
//        // 根据用户角色获取不同范围的统计数据
//        if ("ADMIN".equals(userRole)) {
//            stats = getAdminStats(semester);
//        } else if ("TEACHER".equals(userRole)) {
//            stats = getTeacherStats(semester, userId);
//        } else {
//            stats = getStudentStats(semester, userId);
//        }
//
//        return stats;
//    }
//
//    private DashboardStatsVO getAdminStats(String semester) {
//        DashboardStatsVO stats = new DashboardStatsVO();
//
//        // 课程统计
//        DashboardStatsVO.CourseStats courseStats = new DashboardStatsVO.CourseStats();
//        courseStats.setTotalCount(courseMapper.selectCount(null));
//        
//        LambdaQueryWrapper<Course> activeCourseWrapper = new LambdaQueryWrapper<Course>()
//            .eq(Course::getSemester, semester)
//            .eq(Course::getIsActive, "ACTIVE");
//        courseStats.setActiveCount(courseMapper.selectCount(activeCourseWrapper));
//        
//        LambdaQueryWrapper<Course> completedCourseWrapper = new LambdaQueryWrapper<Course>()
//            .eq(Course::getSemester, semester)
//            .eq(Course::getIsActive, "COMPLETED");
//        courseStats.setCompletedCount(courseMapper.selectCount(completedCourseWrapper));
//        stats.setCourseStats(courseStats);
//
//        // 学生统计
//        DashboardStatsVO.StudentStats studentStats = new DashboardStatsVO.StudentStats();
//        studentStats.setTotalCount(studentMapper.selectCount(null));
//        
//        LambdaQueryWrapper<CourseSelection> activeStudentWrapper = new LambdaQueryWrapper<CourseSelection>()
//            .eq(CourseSelection::getSemester, semester);
//        studentStats.setActiveCount(selectionMapper.selectCount(activeStudentWrapper));
//        
//        studentStats.setAvgGrade(calculateAverageGrade(semester, null));
//        stats.setStudentStats(studentStats);
//
//        // 作业统计
//        DashboardStatsVO.HomeworkStats homeworkStats = new DashboardStatsVO.HomeworkStats();
//        LambdaQueryWrapper<Homework> homeworkWrapper = new LambdaQueryWrapper<Homework>()
//            .eq(Homework::getSemester, semester);
//        homeworkStats.setTotalCount(homeworkMapper.selectCount(homeworkWrapper));
//        
//        LambdaQueryWrapper<HomeworkSubmission> pendingWrapper = new LambdaQueryWrapper<HomeworkSubmission>()
//            .eq(HomeworkSubmission::getStatus, "SUBMITTED")
//            .eq(HomeworkSubmission::getSemester, semester);
//        homeworkStats.setPendingCount(submissionMapper.selectCount(pendingWrapper));
//        
//        LambdaQueryWrapper<HomeworkSubmission> submittedWrapper = new LambdaQueryWrapper<HomeworkSubmission>()
//            .eq(HomeworkSubmission::getSemester, semester);
//        homeworkStats.setSubmittedCount(submissionMapper.selectCount(submittedWrapper));
//        
//        homeworkStats.setAvgScore(calculateHomeworkAvgScore(semester, null));
//        stats.setHomeworkStats(homeworkStats);
//
//        // 成绩统计
//        DashboardStatsVO.GradeStats gradeStats = new DashboardStatsVO.GradeStats();
//        LambdaQueryWrapper<Grade> gradeWrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getSemester, semester);
//        gradeStats.setTotalCount(gradeMapper.selectCount(gradeWrapper));
//        
//        LambdaQueryWrapper<Grade> pendingGradeWrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getSemester, semester)
//            .isNull(Grade::getScore);
//        gradeStats.setPendingCount(gradeMapper.selectCount(pendingGradeWrapper));
//        
//        LambdaQueryWrapper<Grade> publishedGradeWrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getSemester, semester)
//            .isNotNull(Grade::getScore);
//        gradeStats.setPublishedCount(gradeMapper.selectCount(publishedGradeWrapper));
//        
//        gradeStats.setAvgScore(calculateAverageGrade(semester, null));
//        stats.setGradeStats(gradeStats);
//
//        // 最近作业
//        stats.setRecentHomework(getRecentHomework(semester, null));
//
//        // 最近成绩
//        stats.setRecentGrades(getRecentGrades(semester, null));
//
//        return stats;
//    }
//
//    private DashboardStatsVO getTeacherStats(String semester, Long teacherId) {
//        // 类似getAdminStats，但查询条件增加teacherId
//        // 实现省略，可参考getAdminStats方法
//        return new DashboardStatsVO();
//    }
//
//    private DashboardStatsVO getStudentStats(String semester, Long studentId) {
//        // 类似getAdminStats，但查询条件增加studentId
//        // 实现省略，可参考getAdminStats方法
//        return new DashboardStatsVO();
//    }
//
//    private Double calculateAverageGrade(String semester, Long userId) {
//        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getSemester, semester)
//            .isNotNull(Grade::getScore);
//        if (userId != null) {
//            wrapper.eq(Grade::getStudentId, userId);
//        }
//        List<Grade> grades = gradeMapper.selectList(wrapper);
//        if (grades.isEmpty()) {
//            return 0.0;
//        }
//        return grades.stream()
//            .mapToDouble(Grade::getScore)
//            .average()
//            .orElse(0.0);
//    }
//
//    private Double calculateHomeworkAvgScore(String semester, Long userId) {
//        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<HomeworkSubmission>()
//            .eq(HomeworkSubmission::getSemester, semester)
//            .isNotNull(HomeworkSubmission::getScore);
//        if (userId != null) {
//            wrapper.eq(HomeworkSubmission::getStudentId, userId);
//        }
//        List<HomeworkSubmission> submissions = submissionMapper.selectList(wrapper);
//        if (submissions.isEmpty()) {
//            return 0.0;
//        }
//        return submissions.stream()
//            .mapToDouble(HomeworkSubmission::getScore)
//            .average()
//            .orElse(0.0);
//    }
//
//    private List<DashboardStatsVO.RecentHomework> getRecentHomework(String semester, Long userId) {
//        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<Homework>()
//            .eq(Homework::getSemester, semester)
//            .orderByDesc(Homework::getDueDate)
//            .last("LIMIT 10");
//        if (userId != null) {
//            // 如果是教师，查询自己发布的作业
//            // 如果是学生，查询自己需要完成的作业
//            wrapper.eq(Homework::getTeacherId, userId);
//        }
//        List<Homework> homeworkList = homeworkMapper.selectList(wrapper);
//        
//        List<DashboardStatsVO.RecentHomework> result = new ArrayList<>();
//        for (Homework homework : homeworkList) {
//            DashboardStatsVO.RecentHomework recent = new DashboardStatsVO.RecentHomework();
//            recent.setId(homework.getId());
//            recent.setTitle(homework.getTitle());
//            recent.setCourseName(getCourseNameById(homework.getCourseId()));
//            recent.setDueDate(homework.getDueDate().toString());
//            
//            // 统计提交情况
//            LambdaQueryWrapper<HomeworkSubmission> submissionWrapper = new LambdaQueryWrapper<HomeworkSubmission>()
//                .eq(HomeworkSubmission::getHomeworkId, homework.getId());
//            recent.setSubmitCount(submissionMapper.selectCount(submissionWrapper));
//            recent.setTotalCount(getHomeworkTotalStudents(homework.getId()));
//            
//            result.add(recent);
//        }
//        return result;
//    }
//
//    private List<DashboardStatsVO.RecentGrade> getRecentGrades(String semester, Long userId) {
//        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getSemester, semester)
//            .isNotNull(Grade::getScore)
//            .orderByDesc(Grade::getUpdateTime)
//            .last("LIMIT 10");
//        if (userId != null) {
//            wrapper.eq(Grade::getStudentId, userId);
//        }
//        List<Grade> grades = gradeMapper.selectList(wrapper);
//        
//        List<DashboardStatsVO.RecentGrade> result = new ArrayList<>();
//        for (Grade grade : grades) {
//            DashboardStatsVO.RecentGrade recent = new DashboardStatsVO.RecentGrade();
//            recent.setId(grade.getId());
//            recent.setCourseName(getCourseNameById(grade.getCourseId()));
//            recent.setType(grade.getType());
//            recent.setAvgScore(grade.getScore());
//            recent.setPassRate(calculatePassRate(grade.getCourseId(), grade.getType()));
//            recent.setPublishTime(grade.getUpdateTime().toString());
//            result.add(recent);
//        }
//        return result;
//    }
//
//    private String getCourseNameById(Long courseId) {
//        Course course = courseMapper.selectById(courseId);
//        return course != null ? course.getName() : "";
//    }
//
//    private Long getHomeworkTotalStudents(Long homeworkId) {
//        // 获取作业所属班级的总学生数
//        Homework homework = homeworkMapper.selectById(homeworkId);
//        if (homework != null) {
//            LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<CourseSelection>()
//                .eq(CourseSelection::getCourseId, homework.getCourseId());
//            return selectionMapper.selectCount(wrapper);
//        }
//        return 0L;
//    }
//
//    private Double calculatePassRate(Long courseId, String type) {
//        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
//            .eq(Grade::getCourseId, courseId)
//            .eq(Grade::getType, type)
//            .isNotNull(Grade::getScore);
//        List<Grade> grades = gradeMapper.selectList(wrapper);
//        if (grades.isEmpty()) {
//            return 0.0;
//        }
//        long passCount = grades.stream()
//            .filter(grade -> grade.getScore() >= 60)
//            .count();
//        return (double) passCount / grades.size();
//    }
//
//    private String getCurrentSemester() {
//        LocalDateTime now = LocalDateTime.now();
//        int year = now.getYear();
//        int month = now.getMonthValue();
//        return year + "-" + (month >= 2 && month <= 7 ? "春季" : "秋季");
//    }
//}
