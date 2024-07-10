package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.common.CustomException;
import com.chenxiaolani.le_takeaway.dto.SetmealDto;
import com.chenxiaolani.le_takeaway.entity.Setmeal;
import com.chenxiaolani.le_takeaway.entity.SetmealDish;
import com.chenxiaolani.le_takeaway.mapper.SetmealMapper;
import com.chenxiaolani.le_takeaway.service.SetmealDishService;
import com.chenxiaolani.le_takeaway.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    // 新增套餐，保存套餐和菜品的关系
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息， 操作setmeal表， insert操作
        this.save(setmealDto);

        // 保存套餐与菜品的关联信息 操作setmeal_dish表， insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        });
        setmealDishService.saveBatch(setmealDishes);
    }

    // 删除套餐，同时删除套餐和菜品的关系
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new CustomException("删除失败，未提供要删除的ID");
        }
        // 等价于 select count(*) from setmeal where id in (1,2,3) and status =1
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("删除失败，有套餐已经上架，无法删除");
        }
        // 如果没有套餐上架，删除套餐
        this.removeByIds(ids);
        // 删除套餐和菜品的关系,
        // 等价于 delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    // 根据套餐id查询套餐，同时查询套餐和菜品的关系
    @Override
    @Transactional
    public SetmealDto getByIdWithDish(Long id) {
        // 查询套餐的基本信息
        Setmeal setmeal = this.getById(id);
        if (setmeal == null) {
            throw new CustomException("套餐不存在");
        }
        // 把查到的setmeal转换成dto
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 再查询套餐和菜品的关系, 根据setmeal_id查询setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);

        // 把查到的setmealDishes设置到setmealDto里
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    // 更新套餐，同时更新套餐和菜品的关系
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 更新套餐的基本信息
        this.updateById(setmealDto);

        // 先删除原来的套餐和菜品的关系
        // delete from setmeal_dish where setmeal_id = 1
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lambdaQueryWrapper);

        // 再插入新的套餐和菜品的关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        });

        setmealDishService.saveBatch(setmealDishes);
    }
}
