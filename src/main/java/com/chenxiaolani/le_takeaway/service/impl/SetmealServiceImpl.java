package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.dto.SetmealDto;
import com.chenxiaolani.le_takeaway.entity.Setmeal;
import com.chenxiaolani.le_takeaway.entity.SetmealDish;
import com.chenxiaolani.le_takeaway.mapper.SetmealMapper;
import com.chenxiaolani.le_takeaway.service.SetmealDishService;
import com.chenxiaolani.le_takeaway.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

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
}
