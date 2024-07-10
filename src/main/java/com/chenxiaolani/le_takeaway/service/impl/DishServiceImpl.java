package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.common.CustomException;
import com.chenxiaolani.le_takeaway.dto.DishDto;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.entity.DishFlavor;
import com.chenxiaolani.le_takeaway.mapper.DishMapper;
import com.chenxiaolani.le_takeaway.service.DishFlavorService;
import com.chenxiaolani.le_takeaway.service.DishService;
import org.springframework.beans.BeanUtils;
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

    // 根据id查询菜品，同时也要查询菜品口味数据
    @Transactional
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 先查询菜品的基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        // 再查询当前菜品对应的口味数据
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, id);

        List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    // 更新菜品，同时也要更新菜品口味数据，要更新两张表的数据，所以要在service层进行事务管理
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 首先更新dish的表
        this.updateById(dishDto);

        // 根据dish_id去删除dish_flavor表中的数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);

        // 重新插入dish_flavor表中的数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.forEach(flavor -> {
            flavor.setDishId(dishDto.getId());
        });
        dishFlavorService.saveBatch(flavors);
    }

    // 删除菜品，同时也要删除菜品口味数据
    @Override
    public void deleteWithFlavor(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("删除失败，未提供要删除的ID");
        }
        // select count(*) from dish where id in (1,2,3) and status = 1
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("删除失败，有菜品已经上架，无法删除");
        }
        // 如果没有菜品上架，删除菜品
        this.removeByIds(ids);
        // 删除菜品和口味的关系
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
    }
}
