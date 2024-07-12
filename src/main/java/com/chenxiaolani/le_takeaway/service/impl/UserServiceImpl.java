package com.chenxiaolani.le_takeaway.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenxiaolani.le_takeaway.entity.User;
import com.chenxiaolani.le_takeaway.mapper.UserMapper;
import com.chenxiaolani.le_takeaway.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
