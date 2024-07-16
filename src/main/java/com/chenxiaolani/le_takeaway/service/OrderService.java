package com.chenxiaolani.le_takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenxiaolani.le_takeaway.entity.Orders;

public interface OrderService extends IService<Orders> {

    /*
     * 用户下单
     */
    public void submit(Orders orders);
}
