package com.chenxiaolani.le_takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxiaolani.le_takeaway.dto.SetmealDto;
import com.chenxiaolani.le_takeaway.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，保存套餐和菜品的关系
     *
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐和菜品的关系
     *
     * @param ids
     */
    public void deleteWithDish(List<Long> ids);
}
