package com.chenxiaolani.le_takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxiaolani.le_takeaway.dto.DishDto;
import com.chenxiaolani.le_takeaway.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    // 新增菜品，同时也要把菜品口味数据插到菜品口味表
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品，同时也要查询菜品口味数据
    public DishDto getByIdWithFlavor(Long id);

    // 更新菜品，同时也要更新菜品口味数据，要更新两张表的数据，所以要在service层进行事务管理
    public void updateWithFlavor(DishDto dishDto);

    // 删除菜品，同时也要删除菜品口味数据
    public void deleteWithFlavor(List<Long> ids);
}
