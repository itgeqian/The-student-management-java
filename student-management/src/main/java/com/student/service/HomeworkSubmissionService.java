package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.HomeworkSubmission;

import java.util.List;

public interface HomeworkSubmissionService extends IService<HomeworkSubmission> {
    HomeworkSubmission submitHomework(HomeworkSubmission submission);
    HomeworkSubmission updateSubmission(HomeworkSubmission submission);
    HomeworkSubmission gradeHomework(Long submissionId, Integer score, String comment);
    IPage<HomeworkSubmission> getSubmissionsByStudent(Long studentId, Page<HomeworkSubmission> page);
    IPage<HomeworkSubmission> getSubmissionsByHomework(Long homeworkId, Page<HomeworkSubmission> page);
    IPage<HomeworkSubmission> getSubmissionsByStudentAndCourseClass(Long studentId, Long courseClassId, Page<HomeworkSubmission> page);
    Double calculateStudentHomeworkAverage(Long studentId, Long courseClassId);
}
