package com.chenxiaolani.le_takeaway.common;

/**
 * 自定义异常类，继承运行异常类
 */
public class CustomException extends RuntimeException{
    public CustomException(String message) {
        super(message);
    }
}
