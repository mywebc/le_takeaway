package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.dto.DishDto;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.service.CategoryService;
import com.chenxiaolani.le_takeaway.service.DishFlavorService;
import com.chenxiaolani.le_takeaway.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品, 这里的参数是一个DTO，DTO是一个数据传输对象，用于前后端传输数据
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        // 这里要向两个表里插入数据，所以要在service层进行事务管理
        dishService.saveWithFlavor(dishDto);
        return R.success("新增成功");
    }


    /**
     * 分页查询菜品
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        // 构造分页对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 构造查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        // 构造排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询
        dishService.page(pageInfo, lambdaQueryWrapper);

        // 将查询结果拷贝到新的page对象，注意这里的records是空的
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        // 先拿到原来查询的菜品的记录，注意这里没有categoryName
        List<Dish> records = pageInfo.getRecords();
        // 将查询结果转换为DTO,并且里面包含了categoryName
        List<DishDto> list = records.stream().map(item -> {
            DishDto dishDto = new DishDto();
            // 拷贝属性
            BeanUtils.copyProperties(item, dishDto);
            // 获取每个菜品的分类id
            Long categoryId = item.getCategoryId();
            // 根据分类id查询分类
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        // 设置新的records
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }
}
