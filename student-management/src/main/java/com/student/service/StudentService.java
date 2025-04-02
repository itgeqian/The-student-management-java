package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Student;
import java.util.List;

public interface StudentService extends IService<Student> {
    Student createStudent(Student student);
    List<Student> createStudents(List<Student> students);
    Student updateStudent(Student student);
    void deleteStudent(Long id);
    void deleteStudents(List<Long> ids);
    Student getStudentById(Long id);
    Student getStudentByNumber(String studentNumber);
    IPage<Student> getStudents(String name, Page<Student> page);
    IPage<Student> getStudentsByDepartment(Long departmentId, Page<Student> page);
    List<Student> getStudentsByCourseClass(Long courseClassId);
    boolean existsByStudentNumber(String studentNumber);
    void importStudents(String filePath);
    void exportStudents(String filePath);
    /**
     * 获取学生列表
     * @param keyword 关键字（姓名）
     * @param departmentId 部门ID
     * @param page 分页参数
     * @return 分页结果
     */
    IPage<Student> getStudents(String keyword, Long departmentId, Page<Student> page);
}
