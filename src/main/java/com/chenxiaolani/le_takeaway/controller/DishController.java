package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.dto.DishDto;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.entity.DishFlavor;
import com.chenxiaolani.le_takeaway.service.CategoryService;
import com.chenxiaolani.le_takeaway.service.DishFlavorService;
import com.chenxiaolani.le_takeaway.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
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
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

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

        // 更新成功后，删除缓存(清除所有的菜品缓存)
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 只清除当前菜品的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);
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

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDtoByIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(dishDtoByIdWithFlavor);
    }


    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        // 这里要向两个表里插入数据，所以要在service层进行事务管理
        dishService.updateWithFlavor(dishDto);

        // 更新成功后，删除缓存(清除所有的菜品缓存)
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        // 只清除当前菜品的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);

        return R.success("更新成功");
    }

    /**
     * 删除菜品
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除的ids{}", ids);
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功");
    }

    /**
     * 更新售卖状态
     *
     * @param ids    需要更新状态的ID数组
     * @param status 要更新的状态值（0：停售，1：启售）
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateSaleStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        log.info("更新状态为{}的ids{}", status, ids);
        if (ids == null || ids.isEmpty()) {
            return R.error("更新失败，未提供要更新的ID");
        }
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(Long.valueOf(id));
            dish.setStatus(status);
            boolean updated = dishService.updateById(dish);
            if (!updated) {
                return R.error("更新失败id:" + id);
            }
        }
        String action = status == 1 ? "启售" : "停售";
        return R.success(action + "成功");
    }


    /**
     * 根据分类id查询菜品列表
     * 使用dish对象接收参数，方便后期还可以接受其他参数
     *
     * @param dishDto
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getDishByCategoryId(DishDto dishDto) {
        List<DishDto> dishDtoList = null;
        log.info("根据分类id查询菜品列表，categoryId:{}", dishDto.toString());

        // 先读取redis缓存
        // 如果缓存中有数据，直接返回,这里的key可以自己定义
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }
        // 如果不存在，查询数据库，并缓存到redis
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper();
        // 添加查询条件
        lambdaQueryWrapper.eq(dishDto.getCategoryId() != null, Dish::getCategoryId, dishDto.getCategoryId());
        // 查找status=1的菜品
        lambdaQueryWrapper.eq(Dish::getStatus, 1);
        // 添加排序条件
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByAsc(Dish::getUpdateTime);
        // 执行查询
        List<Dish> list = dishService.list(lambdaQueryWrapper);

        // 需要在这个返回的list里面加入一个dto,里面包含了查好的flavors
        dishDtoList = list.stream().map(dish -> {
            DishDto dishDto1 = new DishDto();
            BeanUtils.copyProperties(dish, dishDto1);

            // 菜品id
            Long dishId = dish.getId();
            // 根据菜品id去查找菜品的口味，查dishFlavor表
            List<DishFlavor> dishFlavors = dishFlavorService.list(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dishId));
            dishDto1.setFlavors(dishFlavors);
            return dishDto1;
        }).collect(Collectors.toList());

        // 缓存到redis,设置缓存时间为60分钟
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
