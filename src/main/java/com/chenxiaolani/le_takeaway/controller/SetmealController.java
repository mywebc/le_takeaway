package com.chenxiaolani.le_takeaway.controller;

import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.dto.SetmealDto;
import com.chenxiaolani.le_takeaway.service.SetmealDishService;
import com.chenxiaolani.le_takeaway.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

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
}
