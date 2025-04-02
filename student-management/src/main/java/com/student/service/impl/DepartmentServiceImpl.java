package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.common.BusinessException;
import com.student.entity.Department;
import com.student.mapper.DepartmentMapper;
import com.student.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    @Transactional
    public Department createDepartment(Department department) {
        validateDepartment(department);
        department.setCreateTime(LocalDateTime.now());
        save(department);
        return department;
    }

    @Override
    @Transactional
    public Department updateDepartment(Department department) {
        validateDepartment(department);
        Department existingDepartment = getDepartmentById(department.getId());
        updateById(department);
        return department;
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        removeById(id);
    }

    @Override
    public Department getDepartmentById(Long id) {
        Department department = getById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        return department;
    }

    @Override
    public IPage<Department> getDepartments(String name, Page<Department> page) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Department::getName, name);
        }
        wrapper.orderByAsc(Department::getName);
        return page(page, wrapper);
    }

    private void validateDepartment(Department department) {
        if (!StringUtils.hasText(department.getName())) {
            throw new BusinessException("部门名称不能为空");
        }
        
        // 检查部门名称是否已存在
        LambdaQueryWrapper<Department> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Department::getName, department.getName());
        if (department.getId() != null) {
            nameWrapper.ne(Department::getId, department.getId());
        }
        if (count(nameWrapper) > 0) {
            throw new BusinessException("部门名称已存在");
        }
    }
}
