package com.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.entity.Homework;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeworkMapper extends BaseMapper<Homework> {
    /**
     * 查询教师名下所有作业的学生成绩
     */
    IPage<Homework> selectTeacherGrades(@Param("page") IPage<Homework> page,
                                             @Param("teacherId") Long teacherId,
                                             @Param("studentName") String studentName,
                                             @Param("studentNo") String studentNo,
                                             @Param("courseName") String courseName,
                                             @Param("courseCode") String courseCode);
}
