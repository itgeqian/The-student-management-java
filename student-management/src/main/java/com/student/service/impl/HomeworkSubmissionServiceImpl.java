package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.Homework;
import com.student.entity.HomeworkSubmission;
import com.student.mapper.HomeworkSubmissionMapper;
import com.student.service.HomeworkService;
import com.student.service.HomeworkSubmissionService;
import com.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HomeworkSubmissionServiceImpl extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission> implements HomeworkSubmissionService {

    @Autowired
    @Lazy
    private HomeworkService homeworkService;

    @Autowired
    private StudentService studentService;

    @Override
    @Transactional
    public HomeworkSubmission submitHomework(HomeworkSubmission submission) {
        validateSubmission(submission);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setUpdateTime(LocalDateTime.now());
        save(submission);
        return submission;
    }

    @Override
    @Transactional
    public HomeworkSubmission updateSubmission(HomeworkSubmission submission) {
        validateSubmission(submission);
        HomeworkSubmission existingSubmission = getById(submission.getId());
        if (existingSubmission == null) {
            throw new BusinessException("Homework submission not found");
        }
        submission.setUpdateTime(LocalDateTime.now());
        updateById(submission);
        return submission;
    }

    @Override
    @Transactional
    public HomeworkSubmission gradeHomework(Long submissionId, Integer score, String comment) {
        HomeworkSubmission submission = getById(submissionId);
        if (submission == null) {
            throw new BusinessException("Homework submission not found");
        }
        
        if (score < 0 || score > 100) {
            throw new BusinessException("Score must be between 0 and 100");
        }
        
        submission.setScore(score);
        submission.setComment(comment);
        submission.setUpdateTime(LocalDateTime.now());
        updateById(submission);
        
        return submission;
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByStudent(Long studentId, Page<HomeworkSubmission> page) {
        if (studentService.getById(studentId) == null) {
            throw new BusinessException("Student not found");
        }
        
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getStudentId, studentId)
               .orderByDesc(HomeworkSubmission::getSubmitTime);
        
        return page(page, wrapper);
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByHomework(Long homeworkId, Page<HomeworkSubmission> page) {
        if (homeworkService.getById(homeworkId) == null) {
            throw new BusinessException("Homework not found");
        }
        
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId)
               .orderByDesc(HomeworkSubmission::getSubmitTime);
        
        return page(page, wrapper);
    }

    @Override
    public IPage<HomeworkSubmission> getSubmissionsByStudentAndCourseClass(Long studentId, Long courseClassId, Page<HomeworkSubmission> page) {
        if (studentService.getById(studentId) == null) {
            throw new BusinessException("Student not found");
        }
        
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getStudentId, studentId)
               .inSql(HomeworkSubmission::getHomeworkId, 
                     "SELECT id FROM homework WHERE course_class_id = " + courseClassId)
               .orderByDesc(HomeworkSubmission::getSubmitTime);
        
        return page(page, wrapper);
    }

    @Override
    public Double calculateStudentHomeworkAverage(Long studentId, Long courseClassId) {
        List<HomeworkSubmission> submissions = list(new LambdaQueryWrapper<HomeworkSubmission>()
            .eq(HomeworkSubmission::getStudentId, studentId)
            .inSql(HomeworkSubmission::getHomeworkId, 
                  "SELECT id FROM homework WHERE course_class_id = " + courseClassId)
            .isNotNull(HomeworkSubmission::getScore));
            
        if (submissions.isEmpty()) {
            return 0.0;
        }
        
        double total = submissions.stream()
            .mapToInt(HomeworkSubmission::getScore)
            .sum();
            
        return total / submissions.size();
    }

    private void validateSubmission(HomeworkSubmission submission) {
        if (submission.getHomeworkId() == null) {
            throw new BusinessException("Homework ID cannot be empty");
        }
        if (submission.getStudentId() == null) {
            throw new BusinessException("Student ID cannot be empty");
        }
        if (submission.getAnswer() == null || submission.getAnswer().trim().isEmpty()) {
            throw new BusinessException("Submission answer cannot be empty");
        }
        
        Homework homework = homeworkService.getById(submission.getHomeworkId());
        if (homework == null) {
            throw new BusinessException("Homework not found");
        }
        if (homework.getDeadline() != null && homework.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Homework submission deadline has passed");
        }
        
        if (studentService.getById(submission.getStudentId()) == null) {
            throw new BusinessException("Student not found");
        }
    }
}
