package com.student.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.entity.Department;

public interface DepartmentService extends IService<Department> {
    Department createDepartment(Department department);
    Department updateDepartment(Department department);
    void deleteDepartment(Long id);
    Department getDepartmentById(Long id);
    IPage<Department> getDepartments(String name, Page<Department> page);
}
