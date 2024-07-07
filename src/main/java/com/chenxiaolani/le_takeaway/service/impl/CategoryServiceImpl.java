package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.common.CustomException;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.entity.Employee;
import com.chenxiaolani.le_takeaway.entity.Setmeal;
import com.chenxiaolani.le_takeaway.mapper.CategoryMapper;
import com.chenxiaolani.le_takeaway.mapper.EmployeeMapper;
import com.chenxiaolani.le_takeaway.service.CategoryService;
import com.chenxiaolani.le_takeaway.service.DishService;
import com.chenxiaolani.le_takeaway.service.EmployeeService;
import com.chenxiaolani.le_takeaway.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    /**
     * 删除分类，在删除之前需要判断我们的分类下是否有菜品、套餐。
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 查询当前的分类id是否关联菜品，我们需要调用菜品的service
        // 构造查询条件
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishQueryWrapper);
        if (dishCount > 0) {
            // 如果关联菜品，抛出异常
            throw new CustomException("该分类下有菜品，无法删除");
        }
        // 查询当前的分类id是否关联套餐，我们需要调用套餐的service
        // 构造查询条件
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealCount > 0) {
            // 如果关联套餐，抛出异常
            throw new CustomException("该分类下有套餐，无法删除");
        }

        // 走到这儿就可以正常删除该分类
        super.removeById(id);
    }
}
