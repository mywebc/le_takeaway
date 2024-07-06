package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Employee;
import com.chenxiaolani.le_takeaway.mapper.CategoryMapper;
import com.chenxiaolani.le_takeaway.mapper.EmployeeMapper;
import com.chenxiaolani.le_takeaway.service.CategoryService;
import com.chenxiaolani.le_takeaway.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
