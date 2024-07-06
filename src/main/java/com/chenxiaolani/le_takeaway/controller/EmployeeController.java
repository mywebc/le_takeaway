package com.chenxiaolani.le_takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenxiaolani.le_takeaway.common.R;
import com.chenxiaolani.le_takeaway.entity.Employee;
import com.chenxiaolani.le_takeaway.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

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

    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工信息{}", employee.toString());
        // 设置初始密码
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        // 设置创建时间和更新时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 先从session里获取员工id，注意这里返回默认是Object类型，需要转型为Long
        Long empId = (Long) request.getSession().getAttribute("employee");
        // 设置创建人和更新人
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        // 因为employeeService继承了IService，所以可以直接调用save方法
        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工分页查询, Page类是mybatis plus提供的分页查询类
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询员工信息，page={},pageSize={},name={}", page, pageSize, name);

        // 构造分页查询条件
        Page pageInfo = new Page(page, pageSize);

        // 构造条件查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // like 就是模糊查询，相似的还有其他方法对应sql不同的查询方式
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 构造排序
        queryWrapper.orderByDesc(Employee::getCreateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("修改员工信息{}", employee.toString());
        // 注意这里需要修改更新时间，和更新人
        employee.setUpdateTime(LocalDateTime.now());
        // 这个方法默认会返回一个Object的类型， 这里需要强转为Long
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        // 这里直接调用updateById方法，mybatis plus会根据id自动更新
        employeeService.updateById(employee);
        return R.success("修改员工成功");
    }

    /**
     * 根据ID查询员工消息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("查询员工信息，id={}", id);
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("员工不存在");
    }
}
