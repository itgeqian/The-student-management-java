package com.student.dto;

import com.student.entity.Homework;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeworkWithGradeDTO {
    // 作业基本信息
    private Long id;
    private Long courseClassId;
    private String title;
    private String content;
    private String type;
    private LocalDateTime deadline;
    private LocalDateTime createTime;
    
    // 提交和成绩信息
    private Long submissionId;
    private String submissionContent;
    private String attachmentUrl;
    private Integer score;
    private String comment;
    private String status;
    private LocalDateTime submitTime;
    private LocalDateTime updateTime;

    public static HomeworkWithGradeDTO fromHomework(Homework homework) {
        HomeworkWithGradeDTO dto = new HomeworkWithGradeDTO();
        dto.setId(homework.getId());
        dto.setCourseClassId(homework.getCourseClassId());
        dto.setTitle(homework.getTitle());
        dto.setContent(homework.getContent());
        dto.setType(homework.getType());
        dto.setDeadline(homework.getDeadline());
        dto.setCreateTime(homework.getCreateTime());
        
        // 提交信息
        //dto.setSubmissionId(homework.getSubmissionId());
        dto.setSubmissionContent(homework.getSubmissionContent());
        dto.setAttachmentUrl(homework.getSubmissionAttachment());
        dto.setScore(homework.getSubmissionScore());
        dto.setComment(homework.getSubmissionComment());
        dto.setStatus(homework.getSubmissionStatus());
        dto.setSubmitTime(homework.getSubmissionTime());
        dto.setUpdateTime(homework.getSubmissionUpdateTime());
        
        return dto;
    }
}
