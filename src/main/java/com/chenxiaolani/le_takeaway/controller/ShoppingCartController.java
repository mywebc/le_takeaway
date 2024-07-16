package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenxiaolani.le_takeaway.common.BaseContext;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.entity.ShoppingCart;
import com.chenxiaolani.le_takeaway.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("添加购物车:{}", shoppingCart);
        // 获取当前用户
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 区分到底是菜品还是套餐
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 先设置用户id
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if (dishId != null) {
            // 说明是菜品，设置dishId
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 说明是套餐,设置setmealId
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 根据用户id和菜品id/套餐id, 去查询shopping_cart表， 如果有，number+1， 如果没有就重新插入
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if (cart != null) {
            // 说明购物车中已经有这个菜品了，数量+1
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartService.updateById(cart);
            return R.success(cart);
        } else {
            // 说明shoppingCartService购物车中没有这个菜品，直接插入
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            return R.success(shoppingCart);
        }
    }

    /**
     * 查询购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查询购物车");
        // 根据userId获取对应的菜品和套餐
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     *
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        log.info("清空购物车");
        // 根据userId删除购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    /**
     * 减少购物车
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("减少购物车:{}", shoppingCart);
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        // 区分到底是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            // 说明是菜品，设置dishId
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            // 说明是套餐,设置setmealId
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if (cart != null) {
            // 说明购物车中已经有这个菜品了，数量-1
            cart.setNumber(cart.getNumber() - 1);
            if (cart.getNumber() == 0) {
                // 如果数量为0，直接删除
                shoppingCartService.removeById(cart.getId());
            } else {
                shoppingCartService.updateById(cart);
            }
        }
        return R.success("减少购物车成功");
    }
}
