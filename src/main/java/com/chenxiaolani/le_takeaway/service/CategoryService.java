package com.chenxiaolani.le_takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Employee;

public interface CategoryService extends IService<Category> {
    // 定义一个方法，根据id删除分类
    public void remove(Long id);
}
