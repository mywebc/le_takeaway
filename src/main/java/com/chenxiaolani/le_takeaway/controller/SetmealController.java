package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.dto.SetmealDto;
import com.chenxiaolani.le_takeaway.entity.Category;
import com.chenxiaolani.le_takeaway.entity.Setmeal;
import com.chenxiaolani.le_takeaway.service.CategoryService;
import com.chenxiaolani.le_takeaway.service.SetmealDishService;
import com.chenxiaolani.le_takeaway.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> add(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐{}", setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        log.info("分页查询套餐列表{},{},{}", page, pageSize, name);
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        // 这里面有分类名称的字段，所以用dto
        Page<SetmealDto> pageInfoDto = new Page<>();

        // 构造条件查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 套餐名称模糊查询
        queryWrapper.likeRight(name != null, Setmeal::getName, name);
        // 构造排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        // 转换成dto， records要忽略，因为records是List<Setmeal>，不是List<SetmealDto>，这里我们自己要自己设置records
        BeanUtils.copyProperties(pageInfo, pageInfoDto, "records");
        // 先取原来setmeal里的records
        List<Setmeal> records = pageInfo.getRecords();
        // 循环原来的records,在里面返回新的setmealDto
        List<SetmealDto> list = records.stream().map(setmeal -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            Category category = categoryService.getById(setmeal.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        // 设置新的records
        pageInfoDto.setRecords(list);

        return R.success(pageInfoDto);
    }

    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除的ids{}", ids);
        // 注意： 删除套餐，也要删除套餐关联的菜品
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info("查询套餐{}", id);
        SetmealDto byIdWithDish = setmealService.getByIdWithDish(id);
        return R.success(byIdWithDish);
    }


    /**
     * 更新套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("更新套餐{}", setmealDto.toString());
        // 先更新套餐的基本信息， 然后更新套餐和菜品的关系，所以放到service层进行事务管理
        setmealService.updateWithDish(setmealDto);
        return R.success("更新成功");
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
            Setmeal setmeal = new Setmeal();
            setmeal.setId(Long.valueOf(id));
            setmeal.setStatus(status);


            boolean updated = setmealService.updateById(setmeal);
            if (!updated) {
                return R.error("更新失败id:" + id);
            }
        }
        String action = status == 1 ? "启售" : "停售";
        return R.success(action + "成功");
    }


    /**
     * 根据category_id和status查询对应套餐
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        // 如果status不为空，就加上status的查询条件
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        // 添加排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        if (list != null) {
            return R.success(list);
        }
        return R.error("没有找到该对象");
    }
}
