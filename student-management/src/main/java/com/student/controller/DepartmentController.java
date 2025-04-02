package com.student.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.common.Result;
import com.student.entity.Department;
import com.student.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public Result<IPage<Department>> getDepartments(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Department> page = new Page<>(current, size);
        return Result.success(departmentService.getDepartments(name, page));
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartment(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    @PostMapping
    public Result<Department> createDepartment(@RequestBody Department department) {
        return Result.success(departmentService.createDepartment(department));
    }

    @PutMapping
    public Result<Department> updateDepartment(@RequestBody Department department) {
        return Result.success(departmentService.updateDepartment(department));
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}
