package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.FinalGrade;
import com.student.entity.GradeComposition;
import com.student.entity.Student;
import com.student.entity.CourseClass;
import com.student.mapper.FinalGradeMapper;
import com.student.service.GradeService;
import com.student.service.CourseClassService;
import com.student.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradeServiceImpl extends ServiceImpl<FinalGradeMapper, FinalGrade> implements GradeService {

    @Autowired
    private CourseClassService courseClassService;
    
    @Autowired
    private StudentService studentService;

    @Override
    @Transactional
    public FinalGrade createGrade(FinalGrade grade) {
        validateGrade(grade);
        grade.setCreateTime(LocalDateTime.now());
        grade.setUpdateTime(LocalDateTime.now());
        save(grade);
        return grade;
    }

    @Override
    @Transactional
    public FinalGrade updateGrade(FinalGrade grade) {
        validateGrade(grade);

        FinalGrade existingGrade = getById(grade.getId());
        if (existingGrade == null) {
            throw new BusinessException("Grade not found");
        }
        
        // 检查是否已发布
        if (existingGrade.getIsPublished()) {
            throw new BusinessException("Cannot update published grade");
        }
        
        grade.setUpdateTime(LocalDateTime.now());
        updateById(grade);
        return grade;
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        FinalGrade grade = getById(id);
        if (grade == null) {
            throw new BusinessException("Grade not found");
        }
        
        // 检查是否已发布
        if (grade.getIsPublished()) {
            throw new BusinessException("Cannot delete published grade");
        }
        
        removeById(id);
    }

    @Override
    public FinalGrade getGradeById(Long id) {
        FinalGrade grade = getById(id);
        if (grade == null) {
            throw new BusinessException("Grade not found");
        }
        
        // 填充关联数据
        grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
        grade.setStudent(studentService.getById(grade.getStudentId()));
        
        return grade;
    }

    @Override
    public IPage<FinalGrade> getStudentGrades(Long studentId, Page<FinalGrade> page) {
        // 验证学生是否存在
        if (!studentService.lambdaQuery().eq(Student::getId, studentId).exists()) {
            throw new BusinessException("Student not found");
        }
        
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getStudentId, studentId)
               .orderByDesc(FinalGrade::getCreateTime);
        
        IPage<FinalGrade> gradeIPage = page(page, wrapper);
        
        // 填充关联数据
        gradeIPage.getRecords().forEach(grade -> {
            grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
            grade.setStudent(studentService.getById(grade.getStudentId()));
        });
        
        return gradeIPage;
    }

    @Override
    public IPage<FinalGrade> getCourseClassGrades(Long courseClassId, Page<FinalGrade> page) {
        // 验证课程班级是否存在
        if (!courseClassService.lambdaQuery().eq(CourseClass::getId, courseClassId).exists()) {
            throw new BusinessException("Course class not found");
        }
        
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getCourseClassId, courseClassId)
               .orderByDesc(FinalGrade::getCreateTime);
        
        IPage<FinalGrade> gradeIPage = page(page, wrapper);
        
        // 填充关联数据
        gradeIPage.getRecords().forEach(grade -> {
            grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
            grade.setStudent(studentService.getById(grade.getStudentId()));
        });
        
        return gradeIPage;
    }

    private void validateGrade(FinalGrade grade) {
        if (grade.getCourseClassId() == null) {
            throw new BusinessException("Course class ID cannot be null");
        }
        if (grade.getStudentId() == null) {
            throw new BusinessException("Student ID cannot be null");
        }
        
        // 验证课程班级是否存在
        if (!courseClassService.lambdaQuery().eq(CourseClass::getId, grade.getCourseClassId()).exists()) {
            throw new BusinessException("Course class not found");
        }
        
        // 验证学生是否存在
        if (!studentService.lambdaQuery().eq(Student::getId, grade.getStudentId()).exists()) {
            throw new BusinessException("Student not found");
        }
        
        // 检查是否已存在成绩记录（除了更新的情况）
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getStudentId, grade.getStudentId())
               .eq(FinalGrade::getCourseClassId, grade.getCourseClassId());
        if (grade.getId() != null) {
            wrapper.ne(FinalGrade::getId, grade.getId());
        }
        if (this.getOne(wrapper) != null) {
            throw new BusinessException("Grade already exists for this student and course class");
        }
    }

    @Override
    public FinalGrade getStudentFinalGrade(Long studentId, Long courseClassId) {
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getStudentId, studentId)
               .eq(FinalGrade::getCourseClassId, courseClassId);
        
        FinalGrade grade = getOne(wrapper);
        if (grade != null) {
            grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
            grade.setStudent(studentService.getById(grade.getStudentId()));
        }
        return grade;
    }

    @Override
    public Page<FinalGrade> getClassFinalGrades(Long courseClassId, int current, int size) {
        Page<FinalGrade> page = new Page<>(current, size);
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getCourseClassId, courseClassId)
               .orderByDesc(FinalGrade::getCreateTime);
        
        Page<FinalGrade> gradePage = page(page, wrapper);
        
        // 填充关联数据
        gradePage.getRecords().forEach(grade -> {
            grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
            grade.setStudent(studentService.getById(grade.getStudentId()));
        });
        
        return gradePage;
    }

    @Override
    public List<FinalGrade> searchStudentGrades(Long courseClassId, String studentNumber) {
        // 先通过学号找到学生
        List<Student> students = studentService.lambdaQuery()
            .like(Student::getStudentNo, studentNumber)
            .list();
        
        if (students.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些学生在指定课程班级的成绩
        LambdaQueryWrapper<FinalGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalGrade::getCourseClassId, courseClassId)
               .in(FinalGrade::getStudentId, students.stream().map(Student::getId).collect(Collectors.toList()));
        
        List<FinalGrade> grades = list(wrapper);
        
        // 填充关联数据
        grades.forEach(grade -> {
            grade.setCourseClass(courseClassService.getCourseClassById(grade.getCourseClassId()));
            grade.setStudent(studentService.getById(grade.getStudentId()));
        });
        
        return grades;
    }

    @Override
    public GradeComposition setGradeComposition(Long courseClassId, Integer homeworkPercentage,
                                                Integer experimentPercentage, Integer examPercentage) {
        // 验证课程班级是否存在
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        // 验证百分比总和是否为100
        double total = homeworkPercentage + experimentPercentage + examPercentage;
        if (Math.abs(total - 100.0) > 0.001) {
            throw new BusinessException("Grade composition percentages must sum to 100");
        }
        
        // 更新课程班级的成绩构成
        courseClass.setHomeworkWeight((int)homeworkPercentage);
        courseClass.setExperimentWeight((int)experimentPercentage);
        courseClass.setFinalExamWeight((int)examPercentage);
        courseClassService.updateById(courseClass);
        
        // 返回成绩构成对象
        GradeComposition composition = new GradeComposition();
        composition.setCourseClassId(courseClassId);
        composition.setHomeworkWeight((int)homeworkPercentage);
        composition.setExperimentWeight((int)experimentPercentage);
        composition.setFinalExamWeight((int)examPercentage);
        return composition;
    }

    @Override
    public GradeComposition getGradeComposition(Long courseClassId) {
        CourseClass courseClass = courseClassService.getCourseClassById(courseClassId);
        if (courseClass == null) {
            throw new BusinessException("Course class not found");
        }
        
        GradeComposition composition = new GradeComposition();
        composition.setCourseClassId(courseClassId);
        composition.setHomeworkWeight(courseClass.getHomeworkWeight());
        composition.setExperimentWeight(courseClass.getExperimentWeight());
        composition.setFinalExamWeight(courseClass.getFinalExamWeight());
        return composition;
    }

    @Override
    @Transactional
    public void updateExamGrade(Long studentId, Long courseClassId, Integer score) {
        if (score < 0 || score > 100) {
            throw new BusinessException("Score must be between 0 and 100");
        }
        
        FinalGrade grade = getStudentFinalGrade(studentId, courseClassId);
        if (grade == null) {
            grade = new FinalGrade();
            grade.setStudentId(studentId);
            grade.setCourseClassId(courseClassId);
            grade.setCreateTime(LocalDateTime.now());
        }
        
        grade.setExamScore(score);
        grade.setUpdateTime(LocalDateTime.now());
        saveOrUpdate(grade);
    }

    @Override
    @Transactional
    public void batchUpdateExamGrades(Long courseClassId, List<Map<String, Object>> grades) {
        for (Map<String, Object> gradeInfo : grades) {
            Long studentId = Long.parseLong(gradeInfo.get("studentId").toString());
            Integer score = Integer.parseInt(gradeInfo.get("score").toString());
            updateExamGrade(studentId, courseClassId, score);
        }
    }

    @Override
    public Integer getExamGrade(Long studentId, Long courseClassId) {
        FinalGrade grade = getStudentFinalGrade(studentId, courseClassId);
        return grade != null ? grade.getExamScore() : null;
    }

    @Override
    @Transactional
    public void submitGrades(Long courseClassId) {
        // 检查所有学生是否都有成绩
        List<Student> students = studentService.getStudentsByCourseClass(courseClassId);
        for (Student student : students) {
            FinalGrade grade = getStudentFinalGrade(student.getId(), courseClassId);
            if (grade == null || grade.getExamScore() == null) {
                throw new BusinessException("Not all students have grades");
            }
        }
        
        // 更新所有成绩状态为已提交
        lambdaUpdate()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .set(FinalGrade::getIsSubmitted, true)
            .set(FinalGrade::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    @Transactional
    public void publishGrades(Long courseClassId) {
        // 检查成绩是否已提交
        if (!isGradesSubmitted(courseClassId)) {
            throw new BusinessException("Grades must be submitted before publishing");
        }
        
        // 更新所有成绩状态为已发布
        lambdaUpdate()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .set(FinalGrade::getIsPublished, true)
            .set(FinalGrade::getUpdateTime, LocalDateTime.now())
            .update();
    }

    @Override
    public boolean isGradesPublished(Long courseClassId) {
        return lambdaQuery()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .eq(FinalGrade::getIsPublished, true)
            .exists();
    }

    private boolean isGradesSubmitted(Long courseClassId) {
        return lambdaQuery()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .eq(FinalGrade::getIsSubmitted, true)
            .exists();
    }

    @Override
    public Map<String, Object> getGradeStatistics(Long courseClassId) {
        List<FinalGrade> grades = lambdaQuery()
            .eq(FinalGrade::getCourseClassId, courseClassId)
            .list();
            
        if (grades.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalStudents", 0);
            emptyStats.put("averageScore", 0.0);
            emptyStats.put("maxScore", 0.0);
            emptyStats.put("minScore", 0.0);
            emptyStats.put("passRate", 0.0);
            return emptyStats;
        }
        
        int totalStudents = grades.size();
        double averageScore = grades.stream()
            .filter(g -> g.getTotalScore() != null)
            .mapToDouble(g -> g.getTotalScore().doubleValue())
            .average()
            .orElse(0.0);
            
        double maxScore = grades.stream()
            .filter(g -> g.getTotalScore() != null)
            .mapToDouble(g -> g.getTotalScore().doubleValue())
            .max()
            .orElse(0.0);
            
        double minScore = grades.stream()
            .filter(g -> g.getTotalScore() != null)
            .mapToDouble(g -> g.getTotalScore().doubleValue())
            .min()
            .orElse(0.0);
            
        long passCount = grades.stream()
            .filter(g -> g.getTotalScore() != null && g.getTotalScore().doubleValue() >= 60.0)
            .count();
            
        double passRate = totalStudents > 0 ? (double) passCount / totalStudents * 100 : 0.0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("averageScore", averageScore);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        stats.put("passRate", passRate);
        return stats;
    }
}
