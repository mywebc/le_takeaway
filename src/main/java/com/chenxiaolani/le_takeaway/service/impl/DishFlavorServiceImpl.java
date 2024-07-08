package com.chenxiaolani.le_takeaway.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.entity.Dish;
import com.chenxiaolani.le_takeaway.entity.DishFlavor;
import com.chenxiaolani.le_takeaway.mapper.DishFlavorMapper;
import com.chenxiaolani.le_takeaway.mapper.DishMapper;
import com.chenxiaolani.le_takeaway.service.DishFlavorService;
import com.chenxiaolani.le_takeaway.service.DishService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
