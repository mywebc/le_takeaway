package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.dto.DishDto;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.entity.DishFlavor;
import com.chenxiaolani.le_takeaway.mapper.DishMapper;
import com.chenxiaolani.le_takeaway.service.DishFlavorService;
import com.chenxiaolani.le_takeaway.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时也要把菜品口味数据插到菜品口味表，Transactional注解表示这个方法是一个事务方法，要么全部成功，要么全部失败，保证数据的一致性。
     *
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 先保存菜品的信息到dish表
        this.save(dishDto);

        // 保存菜品口味数据到dish_flavor表
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 设置dishId
        flavors.forEach(flavor -> {
            flavor.setDishId(dishId);
        });
        // 批量保存
        dishFlavorService.saveBatch(flavors);
    }
}
