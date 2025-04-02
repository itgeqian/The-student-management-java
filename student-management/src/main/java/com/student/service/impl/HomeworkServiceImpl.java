package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.dto.GradeRequest;
import com.student.dto.HomeworkWithGradeDTO;
import com.student.entity.*;
import com.student.mapper.CourseClassMapper;
import com.student.mapper.HomeworkMapper;
import com.student.mapper.HomeworkSubmissionMapper;
import com.student.service.*;
import com.student.utils.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements HomeworkService {

    @Autowired
    @Lazy
    private HomeworkSubmissionService homeworkSubmissionService;

    @Autowired
    private HomeworkSubmissionMapper homeworkSubmissionMapper;

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private CourseClassService courseClassService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private FinalGradeService finalGradeService;

    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseClassMapper courseClassMapper;

    @Autowired
    private CommonUtils commonUtils;

    @Override
    @Transactional
    public Homework createHomework(Homework homework) {
        // 参数验证
        //if (homework.getCourseClassId() == null) {
        //    throw new BusinessException("Course class ID cannot be null");
        //}
        if (!StringUtils.hasText(homework.getTitle())) {
            throw new BusinessException("Homework title cannot be empty");
        }
        if (homework.getDeadline() == null) {
            throw new BusinessException("Deadline cannot be null");
        }
        
        save(homework);
        return homework;
    }

    @Override
    @Transactional
    public Homework updateHomework(Homework homework) {
        // 检查作业是否存在
        if (homework.getId() == null) {
            throw new BusinessException("Homework ID cannot be null");
        }
        
        Homework existingHomework = getById(homework.getId());
        if (existingHomework == null) {
            throw new BusinessException("Homework not found");
        }
        
        // 参数验证
        if (!StringUtils.hasText(homework.getTitle())) {
            throw new BusinessException("Homework title cannot be empty");
        }
        
        updateById(homework);
        return homework;
    }

    @Override
    @Transactional
    public void deleteHomework(Long id) {
        removeById(id);
    }

    @Override
    public Homework getHomeworkById(Long id) {
        Homework homework = getById(id);
        if (homework == null) {
            return null;
        }
        
        // 填充课程班级信息
        homework.setCourseClass(courseClassService.getCourseClassById(homework.getCourseClassId()));

        // 获取当前登录用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            // 如果是学生，获取其ID
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            Student student = studentService.getStudentByNumber(userDetails.getUsername());
            if (student != null) {
                // 查询该学生对这个作业的提交记录
                HomeworkSubmission submission = homeworkSubmissionService.lambdaQuery()
                    .eq(HomeworkSubmission::getHomeworkId, id)
                    .eq(HomeworkSubmission::getStudentId, student.getId())
                    .orderByDesc(HomeworkSubmission::getSubmitTime)
                    .last("LIMIT 1")
                    .one();
                
                // 设置提交记录字段
                if (submission != null) {
                    homework.setSubmissionContent(submission.getAnswer());
                    homework.setSubmissionAttachment(submission.getAttachmentUrl());
                    homework.setSubmissionScore(submission.getScore());
                    homework.setSubmissionComment(submission.getComment());
                    homework.setSubmissionStatus(submission.getStatus());
                    homework.setSubmissionTime(submission.getSubmitTime());
                    homework.setSubmissionUpdateTime(submission.getUpdateTime());
                }
                
                // 设置是否需要提交
                boolean isDeadlinePassed = homework.getDeadline() != null && 
                                        homework.getDeadline().isBefore(LocalDateTime.now());
                homework.setNeedSubmit((submission == null || "RETURNED".equals(submission.getStatus())) 
                                     && !isDeadlinePassed);
            }
        }
        
        return homework;
    }

    @Override
    public IPage<Homework> getHomeworksByCourseClass(Long courseClassId, Page<Homework> page) {
        if (courseClassId == null) {
            throw new BusinessException("Course class ID cannot be null");
        }
        
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Homework::getCourseClassId, courseClassId)
              .orderByDesc(Homework::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public IPage<Homework> getHomeworksByTeacher(Long teacherId, String keyword, String type, Long courseId, Page<Homework> page) {
        String userType = commonUtils.getUserType(teacherId);
        // 1. 先获取教师的课程班级ID
        LambdaQueryWrapper<CourseClass> courseClassWrapper = new LambdaQueryWrapper<>();
        // 查询学生的选课信息
        if ("student".equalsIgnoreCase(userType)) {
            List<Long> courseClassIds = courseSelectionService.lambdaQuery()
                    .eq(CourseSelection::getStudentId, teacherId)
                    .select(CourseSelection::getCourseClassId)
                    .list()
                    .stream()
                    .map(CourseSelection::getCourseClassId)
                    .collect(Collectors.toList());
            courseClassWrapper.in(CourseClass::getId, courseClassIds);
        } else if ("teacher".equalsIgnoreCase(userType)) {
            courseClassWrapper.eq(CourseClass::getTeacherId, teacherId);
        }
        
        // 如果指定了课程ID，则只查询该课程的班级
        if (courseId != null) {
            courseClassWrapper.eq(CourseClass::getCourseId, courseId);
        }
        
        List<Long> courseClassIds = courseClassService.list(courseClassWrapper)
                .stream()
                .map(CourseClass::getId)
                .collect(Collectors.toList());

        if (courseClassIds.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize());
        }

        // 2. 查询这些课程班级的作业
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Homework::getCourseClassId, courseClassIds);
        
        // 关键字搜索（标题或内容）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Homework::getTitle, keyword)
                .or()
                .like(Homework::getContent, keyword)
            );
        }
        
        // 作业类型筛选
        if (StringUtils.hasText(type)) {
            wrapper.eq(Homework::getType, type);
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc(Homework::getCreateTime);
        
        // 执行分页查询
        IPage<Homework> homeworkPage = page(page, wrapper);
        
        // 填充统计信息和课程班级信息
        homeworkPage.getRecords().forEach(homework -> {
            boolean isDeadlinePassed = homework.getDeadline() != null &&
                    homework.getDeadline().isBefore(LocalDateTime.now());
            homework.setIsDeadlinePassed(isDeadlinePassed);
            // 获取课程班级信息
            CourseClass courseClass = courseClassService.getCourseClassById(homework.getCourseClassId());
            if (courseClass != null) {
                // 获取课程信息
                Course course = courseService.getById(courseClass.getCourseId());
                courseClass.setCourse(course);
                homework.setCourseClass(courseClass);
                
                // 设置统计信息
                int totalCount = courseSelectionService.countByCourseClass(courseClass.getId()).intValue();
                homework.setTotalCount(totalCount);
                
                int submitCount = homeworkSubmissionMapper.selectCount(
                    new LambdaQueryWrapper<HomeworkSubmission>()
                        .eq(HomeworkSubmission::getHomeworkId, homework.getId())
                ).intValue();
                homework.setSubmitCount(submitCount);
            } else {
                homework.setTotalCount(0);
                homework.setSubmitCount(0);
            }
        });
        
        return homeworkPage;
    }

    @Override
    @Transactional
    public void returnHomework(Long submissionId, String comment) {
        HomeworkSubmission submission = homeworkSubmissionService.getById(submissionId);
        if (submission == null) {
            throw new BusinessException("Homework submission not found");
        }
        
        submission.setStatus("RETURNED");
        submission.setComment(comment);
        submission.setUpdateTime(LocalDateTime.now());
        homeworkSubmissionService.updateById(submission);
    }

    @Override
    public Double calculateStudentHomeworkAverage(Long studentId, Long courseClassId) {
        // 1. 获取该课程班级的所有作业
        List<Homework> homeworks = this.lambdaQuery()
            .eq(Homework::getCourseClassId, courseClassId)
            .list();
            
        if (homeworks.isEmpty()) {
            return 0.0;
        }
        
        // 2. 获取学生的所有作业提交记录
        List<HomeworkSubmission> submissions = homeworkSubmissionService.lambdaQuery()
            .eq(HomeworkSubmission::getStudentId, studentId)
            .in(HomeworkSubmission::getHomeworkId, 
                homeworks.stream().map(Homework::getId).collect(Collectors.toList()))
            .orderByDesc(HomeworkSubmission::getSubmitTime)
            .list();
            
        if (submissions.isEmpty()) {
            return 0.0;
        }
        
        // 3. 按作业ID分组，获取每个作业的最新提交
        Map<Long, HomeworkSubmission> latestSubmissions = submissions.stream()
            .collect(Collectors.groupingBy(
                HomeworkSubmission::getHomeworkId,
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(HomeworkSubmission::getSubmitTime)),
                    opt -> opt.orElse(null)
                )
            ));
            
        // 4. 计算平均分（只考虑已评分的作业）
        double totalScore = 0;
        int gradedCount = 0;
        
        for (HomeworkSubmission submission : latestSubmissions.values()) {
            if (submission != null && submission.getScore() != null) {
                totalScore += submission.getScore();
                gradedCount++;
            }
        }
        
        // 5. 返回平均分，如果没有已评分的作业则返回0
        return gradedCount > 0 ? totalScore / gradedCount : 0.0;
    }

    @Override
    public HomeworkSubmission submitHomework(HomeworkSubmission submission) {
        if (submission.getHomeworkId() == null || submission.getStudentId() == null) {
            throw new BusinessException("Homework ID and Student ID cannot be null");
        }

        // Check if homework exists and deadline hasn't passed
        Homework homework = getById(submission.getHomeworkId());
        if (homework == null) {
            throw new BusinessException("Homework not found");
        }
        
        if (homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline())) {
            throw new BusinessException("Homework submission deadline has passed");
        }

        submission.setStatus("SUBMITTED");
        submission.setSubmitTime(LocalDateTime.now());
        homeworkSubmissionService.save(submission);
        return submission;
    }

    @Override
    public HomeworkSubmission updateSubmission(HomeworkSubmission submission) {
        if (submission.getId() == null) {
            throw new BusinessException("Submission ID cannot be null");
        }

        HomeworkSubmission existingSubmission = homeworkSubmissionService.getById(submission.getId());
        if (existingSubmission == null) {
            throw new BusinessException("Submission not found");
        }

        // Check if homework deadline hasn't passed
        Homework homework = getById(existingSubmission.getHomeworkId());
        if (homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline())) {
            throw new BusinessException("Homework submission deadline has passed");
        }

        submission.setStatus("RESUBMITTED");
        submission.setUpdateTime(LocalDateTime.now());
        homeworkSubmissionService.updateById(submission);
        return submission;
    }

    @Override
    public HomeworkSubmission getSubmissionById(Long id) {
        return homeworkSubmissionService.getById(id);
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByStudent(Long studentId, Page<HomeworkSubmission> page) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getStudentId, studentId)
              .orderByDesc(HomeworkSubmission::getSubmitTime);
        return homeworkSubmissionService.page(page, wrapper);
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByHomework(Long homeworkId, Page<HomeworkSubmission> page) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId)
              .orderByDesc(HomeworkSubmission::getSubmitTime);
        
        IPage<HomeworkSubmission> submissionPage = homeworkSubmissionService.page(page, wrapper);
        
        // 填充学生信息
        submissionPage.getRecords().forEach(submission -> {
            submission.setStudent(studentService.getById(submission.getStudentId()));
        });
        
        return submissionPage;
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByStudentAndCourseClass(Long studentId, Long courseClassId, Page<HomeworkSubmission> page) {
        // First get all homework IDs for the course class
        LambdaQueryWrapper<Homework> homeworkWrapper = new LambdaQueryWrapper<>();
        homeworkWrapper.eq(Homework::getCourseClassId, courseClassId);
        List<Homework> homeworks = list(homeworkWrapper);
        List<Long> homeworkIds = homeworks.stream().map(Homework::getId).collect(Collectors.toList());

        if (homeworkIds.isEmpty()) {
            return new Page<>();
        }

        // Then get submissions for these homeworks by the student
        LambdaQueryWrapper<HomeworkSubmission> submissionWrapper = new LambdaQueryWrapper<>();
        submissionWrapper.eq(HomeworkSubmission::getStudentId, studentId)
                        .in(HomeworkSubmission::getHomeworkId, homeworkIds)
                        .orderByDesc(HomeworkSubmission::getSubmitTime);
        return homeworkSubmissionService.page(page, submissionWrapper);
    }

    @Override
    @Transactional
    public HomeworkSubmission gradeHomework(Long submissionId, Integer score, String comment) {
        if (score < 0 || score > 100) {
            throw new BusinessException("Score must be between 0 and 100");
        }

        HomeworkSubmission submission = homeworkSubmissionService.getById(submissionId);
        if (submission == null) {
            throw new BusinessException("Homework submission not found");
        }

        // 获取作业信息
        Homework homework = getById(submission.getHomeworkId());
        if (homework == null) {
            throw new BusinessException("Homework not found");
        }

        submission.setScore(score);
        submission.setComment(comment);
        submission.setStatus("GRADED");
        submission.setUpdateTime(LocalDateTime.now());
        homeworkSubmissionService.updateById(submission);

        // 如果是期末考试，计算并更新总成绩
        if ("EXAM".equals(homework.getType())) {
            // 1. 获取课程班级信息
            CourseClass courseClass = courseClassService.getCourseClassById(homework.getCourseClassId());
            if (courseClass == null) {
                throw new BusinessException("Course class not found");
            }

            // 2. 获取该学生在这个课程班级的最终成绩记录
            FinalGrade finalGrade = finalGradeService.lambdaQuery()
                .eq(FinalGrade::getCourseClassId, homework.getCourseClassId())
                .eq(FinalGrade::getStudentId, submission.getStudentId())
                .one();

            if (finalGrade == null) {
                // 如果不存在则创建新记录
                finalGrade = new FinalGrade();
                finalGrade.setCourseClassId(homework.getCourseClassId());
                finalGrade.setStudentId(submission.getStudentId());
            }

            // 4. 获取该学生在这个课程班级的所有作业提交记录
            List<Homework> classHomeworks = this.lambdaQuery()
                .eq(Homework::getCourseClassId, homework.getCourseClassId())
                .list();
            
            Map<Long, HomeworkSubmission> submissionMap = homeworkSubmissionService.lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, submission.getStudentId())
                .in(HomeworkSubmission::getHomeworkId, 
                    classHomeworks.stream().map(Homework::getId).collect(Collectors.toList()))
                .orderByDesc(HomeworkSubmission::getSubmitTime)
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                    HomeworkSubmission::getHomeworkId,
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparing(HomeworkSubmission::getSubmitTime)),
                        opt -> opt.orElse(null)
                    )
                ));
            
            // 5. 按作业类型分组计算平均分
            Map<String, List<Integer>> scoresByType = new HashMap<>();
            for (Homework h : classHomeworks) {
                HomeworkSubmission s = submissionMap.get(h.getId());
                if (s != null && s.getScore() != null) {
                    scoresByType.computeIfAbsent(h.getType(), k -> new ArrayList<>())
                            .add(s.getScore());
                }
            }

            // 6. 计算各类型作业的平均分
            if (!scoresByType.isEmpty()) {
                // 计算普通作业平均分
                List<Integer> homeworkScores = scoresByType.get("HOMEWORK");
                if (homeworkScores != null && !homeworkScores.isEmpty()) {
                    finalGrade.setHomeworkScore(
                        (int) homeworkScores.stream()
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0)
                    );
                }

                // 计算实验作业平均分
                List<Integer> experimentScores = scoresByType.get("EXPERIMENT");
                if (experimentScores != null && !experimentScores.isEmpty()) {
                    finalGrade.setExperimentScore(
                        (int) experimentScores.stream()
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0)
                    );
                }

                // 查询出来期末考试的总成绩
                List<Integer> examScores = scoresByType.get("EXAM");
                if (examScores == null) {
                    examScores = new ArrayList<>();
                }
                examScores.add(score);
                // 设置考试成绩
                finalGrade.setExamScore(
                        (int) examScores.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                );
            }

            if (finalGrade.getHomeworkScore() == null) {
                finalGrade.setHomeworkScore(0);
            }
            if (finalGrade.getExperimentScore() == null) {
                finalGrade.setExperimentScore(0);
            }
            if (finalGrade.getExamScore() == null) {
                finalGrade.setExamScore(0);
            }
            // 7. 使用课程班级的权重计算总成绩
            int totalScore = (int) (
                (finalGrade.getHomeworkScore() * courseClass.getHomeworkWeight() / 100.0) +
                (finalGrade.getExperimentScore() * courseClass.getExperimentWeight() / 100.0) +
                (finalGrade.getExamScore() * courseClass.getFinalExamWeight() / 100.0)
            );
            finalGrade.setTotalScore(totalScore);
            finalGrade.setIsSubmitted(true);

            // 8. 保存或更新最终成绩
            finalGradeService.saveOrUpdate(finalGrade);
        }

        return submission;
    }

    @Override
    @Transactional
    public void batchGradeHomework(List<GradeRequest> gradeRequests) {
        for (GradeRequest request : gradeRequests) {
            HomeworkSubmission submission = homeworkSubmissionService.getById(request.getSubmissionId());
            if (submission == null) {
                throw new BusinessException("Submission not found: " + request.getSubmissionId());
            }
            
            // 更新作业提交状态
            submission.setScore(request.getScore());
            submission.setComment(request.getComment());
            submission.setStatus("GRADED");
            submission.setUpdateTime(LocalDateTime.now());
            
            homeworkSubmissionService.updateById(submission);
        }
    }

    @Override
    public IPage<FinalGrade> getTotalScore(Long userId, Page<FinalGrade> page) {
        // 1. 获取当前登录教师
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new BusinessException("未登录或登录已过期");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 2. 获取教师所有的课程班级ID
        List<Long> courseClassIds = courseClassService.lambdaQuery()
            .select(CourseClass::getId)
            .eq(CourseClass::getTeacherId, userId)
            .list()
            .stream()
            .map(CourseClass::getId)
            .collect(Collectors.toList());
            
        if (courseClassIds.isEmpty()) {
            return new Page<>();
        }
        
        // 3. 从数据库分页查询最终成绩
        Page<FinalGrade> finalGradePage = finalGradeService.lambdaQuery()
            .in(FinalGrade::getCourseClassId, courseClassIds)
            .orderByDesc(FinalGrade::getUpdateTime)
            .page(page);
        
        if (finalGradePage.getRecords().isEmpty()) {
            return finalGradePage;
        }

        // 4. 获取所有涉及的课程班级和学生ID
        Set<Long> involvedCourseClassIds = finalGradePage.getRecords().stream()
            .map(FinalGrade::getCourseClassId)
            .collect(Collectors.toSet());
        Set<Long> involvedStudentIds = finalGradePage.getRecords().stream()
            .map(FinalGrade::getStudentId)
            .collect(Collectors.toSet());

        // 5. 批量获取课程班级信息
        Map<Long, CourseClass> courseClassMap = courseClassService.listByIds(involvedCourseClassIds)
            .stream()
            .collect(Collectors.toMap(CourseClass::getId, courseClass -> courseClass));

        // 6. 批量获取学生信息
        Map<Long, Student> studentMap = studentService.listByIds(involvedStudentIds)
            .stream()
            .collect(Collectors.toMap(Student::getId, student -> student));

        // 7. 填充课程班级和学生信息
        finalGradePage.getRecords().forEach(grade -> {
            grade.setCourseClass(courseClassMap.get(grade.getCourseClassId()));
            grade.setStudent(studentMap.get(grade.getStudentId()));
        });
        
        // 8. 获取这些学生的作业提交记录
        List<Long> studentIds = finalGradePage.getRecords().stream()
            .map(FinalGrade::getStudentId)
            .collect(Collectors.toList());
        
        // 9. 获取所有课程班级的作业列表
        List<Homework> homeworks = this.lambdaQuery()
            .in(Homework::getCourseClassId, courseClassIds)
            .list();
        
        if (!homeworks.isEmpty()) {
            // 获取最新的提交记录
            Map<Long, Map<Long, HomeworkSubmission>> submissionsByStudent = 
                homeworkSubmissionService.lambdaQuery()
                    .in(HomeworkSubmission::getStudentId, studentIds)
                    .in(HomeworkSubmission::getHomeworkId, 
                        homeworks.stream().map(Homework::getId).collect(Collectors.toList()))
                    .orderByDesc(HomeworkSubmission::getSubmitTime)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(
                        HomeworkSubmission::getStudentId,
                        Collectors.groupingBy(
                            HomeworkSubmission::getHomeworkId,
                            Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(HomeworkSubmission::getSubmitTime)),
                                opt -> opt.orElse(null)
                            )
                        )
                    ));
            
            // 更新每个学生的成绩
            for (FinalGrade finalGrade : finalGradePage.getRecords()) {
                Map<Long, HomeworkSubmission> studentSubmissions = 
                    submissionsByStudent.getOrDefault(finalGrade.getStudentId(), Collections.emptyMap());
                
                // 按作业类型分组计算平均分
                Map<String, List<Integer>> scoresByType = new HashMap<>();
                
                for (Homework homework : homeworks) {
                    // 只处理同一个课程班级的作业
                    if (!homework.getCourseClassId().equals(finalGrade.getCourseClassId())) {
                        continue;
                    }
                    
                    HomeworkSubmission submission = studentSubmissions.get(homework.getId());
                    if (submission != null && submission.getScore() != null) {
                        scoresByType.computeIfAbsent(homework.getType(), k -> new ArrayList<>())
                                .add(submission.getScore());
                    }
                }
                
                // 计算各类型作业的平均分
                if (!scoresByType.isEmpty()) {
                    // 计算普通作业平均分
                    List<Integer> homeworkScores = scoresByType.get("HOMEWORK");
                    if (homeworkScores != null && !homeworkScores.isEmpty()) {
                        finalGrade.setHomeworkScore(
                            (int) homeworkScores.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        );
                    }
                    
                    // 计算实验作业平均分
                    List<Integer> experimentScores = scoresByType.get("EXPERIMENT");
                    if (experimentScores != null && !experimentScores.isEmpty()) {
                        finalGrade.setExperimentScore(
                            (int) experimentScores.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        );
                    }
                }
                
                // 更新最终成绩
                finalGradeService.updateById(finalGrade);
            }
        }
        
        return finalGradePage;
    }

    @Override
    public IPage<Homework> getStudentAvailableHomeworks(Long studentId, Page<Homework> page) {
        List<CourseSelection> CourseSelectionList = courseSelectionService.lambdaQuery()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, "SELECTED")
                .list();

        // 收集CourseSelectionList的课程班级的id集合
        List<Long> courseClassIds = CourseSelectionList.stream()
                .map(CourseSelection::getCourseClassId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(courseClassIds)) {
            courseClassIds.add(-1L);
        }
        // 1. 获取所有作业
        IPage<Homework> homeworkPage = this.baseMapper.selectPage(page,
                new LambdaQueryWrapper<Homework>()
                        .in(Homework::getCourseClassId, courseClassIds)
                        .orderByDesc(Homework::getCreateTime));

        if (homeworkPage.getRecords().isEmpty()) {
            return homeworkPage;
        }

        // 2. 获取学生的所有作业提交记录
        List<HomeworkSubmission> submissions = homeworkSubmissionService.lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .in(HomeworkSubmission::getHomeworkId, 
                    homeworkPage.getRecords().stream().map(Homework::getId).collect(Collectors.toList()))
                .ne(HomeworkSubmission::getStatus, "RETURNED")
                .list();

        // 3. 创建作业ID到提交记录的映射
        Map<Long, HomeworkSubmission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(HomeworkSubmission::getHomeworkId, s -> s));

        // 4. 设置每个作业的提交状态
        homeworkPage.getRecords().forEach(homework -> {
            // 填充课程班级信息
            homework.setCourseClass(courseClassService.getCourseClassById(homework.getCourseClassId()));
            
            HomeworkSubmission submission = submissionMap.get(homework.getId());
            boolean isDeadlinePassed = homework.getDeadline() != null && 
                                     homework.getDeadline().isBefore(LocalDateTime.now());
            
            // 如果已经提交且未被退回，或者已经过了截止日期，则不需要提交
            homework.setNeedSubmit(submission == null && !isDeadlinePassed);
        });

        return homeworkPage;
    }

    @Override
    public IPage<HomeworkWithGradeDTO> getHomeworksWithGrade(Long homeworkId) {
        // 1. 获取作业
        Homework homework = this.getById(homeworkId);
        if (homework == null) {
            return new Page<>();
        }
        
        // 2. 获取该作业的所有提交记录
        List<HomeworkSubmission> submissions = homeworkSubmissionService.lambdaQuery()
            .eq(HomeworkSubmission::getHomeworkId, homeworkId)
            .orderByDesc(HomeworkSubmission::getSubmitTime)
            .list();
            
        // 3. 设置提交信息
        if (!submissions.isEmpty()) {
            HomeworkSubmission submission = submissions.get(0); // 获取最新提交
            //homework.setSubmissionId(submission.getId());
            homework.setSubmissionContent(submission.getAnswer());
            homework.setSubmissionAttachment(submission.getAttachmentUrl());
            homework.setSubmissionScore(submission.getScore());
            homework.setSubmissionComment(submission.getComment());
            homework.setSubmissionStatus(submission.getStatus());
            homework.setSubmissionTime(submission.getSubmitTime());
            homework.setSubmissionUpdateTime(submission.getUpdateTime());
        }
        
        // 4. 转换为DTO
        List<HomeworkWithGradeDTO> records = Collections.singletonList(HomeworkWithGradeDTO.fromHomework(homework));
        Page<HomeworkWithGradeDTO> page = new Page<>(1, 1, 1);
        page.setRecords(records);
        
        return page;
    }

    @Override
    public IPage<FinalGrade> getPublishedScores(Long courseClassId, Page<FinalGrade> page) {
        // 1. 验证课程班级是否存在
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }

        // 2. 先获取该课程班级所有已发布的成绩，用于计算排名
        List<FinalGrade> allGrades = finalGradeService.lambdaQuery()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .eq(FinalGrade::getIsPublished, true)
            .orderByDesc(FinalGrade::getTotalScore)
            .list();

        // 3. 创建总分到排名的映射
        Map<Integer, Integer> scoreRankMap = new HashMap<>();
        int currentRank = 1;
        Integer lastScore = null;
        
        for (FinalGrade grade : allGrades) {
            if (lastScore == null || !lastScore.equals(grade.getTotalScore())) {
                lastScore = grade.getTotalScore();
                scoreRankMap.put(lastScore, currentRank);
            }
            currentRank++;
        }

        // 4. 获取分页数据
        Page<FinalGrade> finalGradePage = finalGradeService.lambdaQuery()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .eq(FinalGrade::getIsPublished, true)
            .orderByDesc(FinalGrade::getTotalScore)
            .page(page);

        if (finalGradePage.getRecords().isEmpty()) {
            return finalGradePage;
        }

        // 5. 获取所有学生ID
        Set<Long> studentIds = finalGradePage.getRecords().stream()
            .map(FinalGrade::getStudentId)
            .collect(Collectors.toSet());

        // 6. 批量获取学生信息
        Map<Long, Student> studentMap = studentService.listByIds(studentIds)
            .stream()
            .collect(Collectors.toMap(Student::getId, student -> student));

        // 7. 填充课程班级、学生信息和排名
        int finalCurrentRank = currentRank;
        finalGradePage.getRecords().forEach(grade -> {
            grade.setCourseClass(courseClass);
            grade.setStudent(studentMap.get(grade.getStudentId()));
            grade.setRank(scoreRankMap.getOrDefault(grade.getTotalScore(), finalCurrentRank));
        });

        return finalGradePage;
    }

    @Override
    public IPage<HomeworkSubmission> getStudentAllHomeworkGrades(Long studentId, Page<HomeworkSubmission> page) {
        // 1. 验证学生是否存在
        Student student = studentService.getById(studentId);
        if (student == null) {
            throw new BusinessException("Student not found");
        }

        // 2. 获取该学生的所有作业提交记录
        Page<HomeworkSubmission> submissionPage = homeworkSubmissionService.lambdaQuery()
            .eq(HomeworkSubmission::getStudentId, studentId)
            .orderByDesc(HomeworkSubmission::getSubmitTime)
            .page(page);

        if (submissionPage.getRecords().isEmpty()) {
            return submissionPage;
        }

        // 3. 获取所有相关的作业ID
        List<Long> homeworkIds = submissionPage.getRecords().stream()
            .map(HomeworkSubmission::getHomeworkId)
            .collect(Collectors.toList());

        // 4. 批量获取作业信息
        Map<Long, Homework> homeworkMap = this.listByIds(homeworkIds)
            .stream()
            .collect(Collectors.toMap(Homework::getId, h -> h));

        // 5. 获取所有相关的课程班级ID
        Set<Long> courseClassIds = homeworkMap.values().stream()
            .map(Homework::getCourseClassId)
            .collect(Collectors.toSet());

        // 6. 批量获取课程班级信息
        Map<Long, CourseClass> courseClassMap = courseClassService.listByIds(courseClassIds)
            .stream()
            .collect(Collectors.toMap(CourseClass::getId, cc -> cc));

        // 7. 填充关联信息
        submissionPage.getRecords().forEach(submission -> {
            // 设置学生信息
            submission.setStudent(student);
            
            // 设置作业信息
            Homework homework = homeworkMap.get(submission.getHomeworkId());
            if (homework != null) {
                homework.setCourseClass(courseClassMap.get(homework.getCourseClassId()));
                submission.setHomework(homework);
            }
        });
        
        return submissionPage;
    }

    @Override
    public Map<String, Long> getSubmissionStats(Long courseId, Long teacherId, Long studentId) {
        Map<String, Long> stats = new HashMap<>();
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        
        if (studentId != null) {
            wrapper.eq(HomeworkSubmission::getStudentId, studentId);
        }
        
        // 计算已提交数量
        stats.put("submitted", homeworkSubmissionService.count(wrapper));
        
        // 计算待批改数量
        wrapper.eq(HomeworkSubmission::getStatus, "SUBMITTED");
        stats.put("pending", homeworkSubmissionService.count(wrapper));
        
        return stats;
    }
    
    @Override
    public Double getAverageScore(Long courseId, Long teacherId, Long studentId) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        
        
        if (studentId != null) {
            wrapper.eq(HomeworkSubmission::getStudentId, studentId);
        }
        
        wrapper.isNotNull(HomeworkSubmission::getScore);
        
        List<HomeworkSubmission> submissions = homeworkSubmissionService.list(wrapper);
        if (submissions.isEmpty()) {
            return 0.0;
        }
        
        double sum = submissions.stream()
                .mapToInt(HomeworkSubmission::getScore)
                .sum();
        return sum / submissions.size();
    }
    
    @Override
    public Map<String, Long> getScoreDistribution(Long courseId, Long teacherId, Long studentId) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        
        if (studentId != null) {
            wrapper.eq(HomeworkSubmission::getStudentId, studentId);
        }
        
        wrapper.isNotNull(HomeworkSubmission::getScore);
        
        List<HomeworkSubmission> submissions = homeworkSubmissionService.list(wrapper);
        Map<String, Long> distribution = new HashMap<>();
        
        submissions.forEach(submission -> {
            String grade = getGradeLevel(submission.getScore());
            distribution.merge(grade, 1L, Long::sum);
        });
        
        return distribution;
    }
    
    @Override
    public List<Map<String, Object>> getSubmissionTrend(Long courseId, Long teacherId, Long studentId, 
            LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        
        if (studentId != null) {
            wrapper.eq(HomeworkSubmission::getStudentId, studentId);
        }
        
        if (startTime != null) {
            wrapper.ge(HomeworkSubmission::getSubmitTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(HomeworkSubmission::getSubmitTime, endTime);
        }
        
        wrapper.orderByAsc(HomeworkSubmission::getSubmitTime);
        
        List<HomeworkSubmission> submissions = homeworkSubmissionService.list(wrapper);
        List<Map<String, Object>> trend = new ArrayList<>();
        
        // 按天统计提交数量
        Map<LocalDate, Long> dailyStats = submissions.stream()
                .collect(Collectors.groupingBy(
                    s -> s.getSubmitTime().toLocalDate(),
                    Collectors.counting()
                ));
        
        dailyStats.forEach((date, count) -> {
            Map<String, Object> point = new HashMap<>();
            point.put("date", date);
            point.put("count", count);
            trend.add(point);
        });
        
        return trend;
    }
    
    private String getGradeLevel(Integer score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    @Override
    public IPage<HomeworkSubmission> getTeacherGrades(Long teacherId, String homeworkType, String studentName, String studentNo, 
            String courseName, String courseCode, Page<HomeworkSubmission> page) {
        // 1. 查询教师的课程班级
        List<Long> courseClassIds = courseClassService.lambdaQuery()
            .eq(CourseClass::getTeacherId, teacherId)
            .list()
            .stream()
            .map(CourseClass::getId)
            .collect(Collectors.toList());
        
        if (courseClassIds.isEmpty()) {
            return new Page<>();
        }
        
        // 2. 构建查询条件
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        
        // 关联查询作业和课程班级
        wrapper.inSql(HomeworkSubmission::getHomeworkId, 
            "SELECT id FROM homework WHERE course_class_id IN (" + 
            StringUtils.collectionToCommaDelimitedString(courseClassIds) + ")"
        );
        
        // 作业类型过滤
        if (StringUtils.hasText(homeworkType)) {
            wrapper.inSql(HomeworkSubmission::getHomeworkId, 
                "SELECT id FROM homework WHERE type = '" + homeworkType + "'"
            );
        }
        
        // 学生姓名过滤
        if (StringUtils.hasText(studentName)) {
            wrapper.inSql(HomeworkSubmission::getStudentId, 
                "SELECT id FROM student WHERE name LIKE '%" + studentName + "%'"
            );
        }
        
        // 学号过滤
        if (StringUtils.hasText(studentNo)) {
            wrapper.inSql(HomeworkSubmission::getStudentId, 
                "SELECT id FROM student WHERE student_no LIKE '%" + studentNo + "%'"
            );
        }
        
        // 课程名称过滤
        if (StringUtils.hasText(courseName)) {
            wrapper.inSql(HomeworkSubmission::getHomeworkId, 
                "SELECT id FROM homework WHERE course_class_id IN (" +
                "SELECT id FROM course_class WHERE course_id IN (" +
                "SELECT id FROM course WHERE name LIKE '%" + courseName + "%'))"
            );
        }
        
        // 课程编码过滤
        if (StringUtils.hasText(courseCode)) {
            wrapper.inSql(HomeworkSubmission::getHomeworkId, 
                "SELECT id FROM homework WHERE course_class_id IN (" +
                "SELECT id FROM course_class WHERE course_id IN (" +
                "SELECT id FROM course WHERE code LIKE '%" + courseCode + "%'))"
            );
        }
        
        // 按提交时间倒序排序
        wrapper.orderByDesc(HomeworkSubmission::getSubmitTime);
        
        // 3. 执行分页查询
        IPage<HomeworkSubmission> submissionPage = homeworkSubmissionService.page(page, wrapper);
        
        // 4. 填充关联信息
        submissionPage.getRecords().forEach(submission -> {
            // 查询作业信息
            Homework homework = this.getById(submission.getHomeworkId());
            if (homework != null) {
                // 查询课程班级信息
                CourseClass courseClass = courseClassService.getCourseClassById(homework.getCourseClassId());
                if (courseClass != null) {
                    // 查询课程信息
                    Course course = courseService.getById(courseClass.getCourseId());
                    courseClass.setCourse(course);
                    homework.setCourseClass(courseClass);
                }
                submission.setHomework(homework);
            }
            
            // 查询学生信息
            Student student = studentService.getById(submission.getStudentId());
            submission.setStudent(student);
        });
        
        return submissionPage;
    }

    @Override
    @Transactional
    public void updateTeacherGradeComposition(Long teacherId, GradeComposition gradeComposition) {
        // 获取教师所有的课程班级
        List<CourseClass> courseClasses = courseClassService.list(
                new LambdaQueryWrapper<CourseClass>()
                        .eq(CourseClass::getTeacherId, teacherId)
        );

        if (courseClasses.isEmpty()) {
            throw new BusinessException("教师没有任何课程班级");
        }

        // 验证权重之和是否为100
        int totalWeight = gradeComposition.getHomeworkWeight() +
                gradeComposition.getExperimentWeight() +
                gradeComposition.getFinalExamWeight();
        if (totalWeight != 100) {
            throw new BusinessException("成绩权重之和必须为100");
        }

        // 更新所有课程班级的成绩构成
        for (CourseClass courseClass : courseClasses) {
            courseClass.setHomeworkWeight(gradeComposition.getHomeworkWeight());
            courseClass.setExperimentWeight(gradeComposition.getExperimentWeight());
            courseClass.setFinalExamWeight(gradeComposition.getFinalExamWeight());
        }

        // 批量更新课程班级
        courseClassService.updateBatchById(courseClasses);
    }

    @Override
    public GradeComposition getTeacherGradeComposition(Long teacherId) {
        // 获取教师所有的课程班级
        List<CourseClass> courseClasses = courseClassService.list(
                new LambdaQueryWrapper<CourseClass>()
                        .eq(CourseClass::getTeacherId, teacherId)
        );

        if (courseClasses.isEmpty()) {
            GradeComposition composition = new GradeComposition();
            composition.setHomeworkWeight(-1);
            composition.setExperimentWeight(-1);
            composition.setFinalExamWeight(-1);
            return composition;
        }

        // 获取第一个课程班级的成绩构成
        CourseClass firstClass = courseClasses.get(0);
        GradeComposition composition = new GradeComposition();
        composition.setCourseClassId(firstClass.getId());
        composition.setHomeworkWeight(firstClass.getHomeworkWeight());
        composition.setExperimentWeight(firstClass.getExperimentWeight());
        composition.setFinalExamWeight(firstClass.getFinalExamWeight());

        // 检查其他课程班级的成绩构成是否一致
        boolean allSame = courseClasses.stream()
                .skip(1)  // 跳过第一个
                .filter(Objects::nonNull)
                .allMatch(comp ->
                        Objects.equals(comp.getHomeworkWeight(), composition.getHomeworkWeight()) &&
                        Objects.equals(comp.getExperimentWeight(), composition.getExperimentWeight()) &&
                        Objects.equals(comp.getFinalExamWeight(), composition.getFinalExamWeight())
                );

        // 如果所有成绩构成一致，返回第一个
        if (allSame) {
            return composition;
        }

        // 如果成绩构成不一致，返回全0的成绩构成
        GradeComposition zeroComposition = new GradeComposition();
        zeroComposition.setHomeworkWeight(0);
        zeroComposition.setExperimentWeight(0);
        zeroComposition.setFinalExamWeight(0);
        return zeroComposition;
    }
}
