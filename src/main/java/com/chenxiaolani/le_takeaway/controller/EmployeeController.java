package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.entity.Employee;
import com.chenxiaolani.le_takeaway.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1. 将明文密码加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 2. 根据用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        // 因为username在数据库已经设置为唯一索引，这里可以使用getOne方法
        Employee emp = employeeService.getOne(queryWrapper);
        // 3. 判断用户是否存在
        if (emp == null) {
            return R.error("用户不存在");
        }
        // 4. 密码是否正确
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }
        // 5. 查看员工状态,1没有禁用，0已经禁用
        if (emp.getStatus() == 0) {
            return R.error("员工已禁用");
        }
        // 6. 登录成功，将用户id存入session
        request.getSession().setAttribute("employee", emp.getId());
        // 最后返回
        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
}
