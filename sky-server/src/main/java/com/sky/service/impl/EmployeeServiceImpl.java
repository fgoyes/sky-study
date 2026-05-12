package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // ！！！对接收密码进行m5加密后在进行密码比对，因为数据库中的密码是加密的
        password = DigestUtils.md5DigestAsHex(password.getBytes()); // password.getBytes() 将密码转换成字节数组后再作为参数进行加密
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 创建员工对象
        Employee employee = new Employee();

        // 属性拷贝（spring中的工具类）左拷贝到右
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置账号的状态，默认正常状态 1正常 0锁定
        // StatusConstant 存储账号状态的常量类
        // StatusConstant.ENABLE 1正常
        // StatusConstant.DISABLE 0锁定
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码123456（对密码进行md5加密处理）
        // PasswordConstant.DEFAULT_PASSWORD 存储类默认密码（123456）的常量类
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前时间为创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        // 设置当前记录的创建人id和修改人id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //  pageHelper分页插件
        //  pageHelper会通过threadlocal线程存储页码和页数，自动为sql操作添加limit查询操作，sql操作只需要查询select from employee;基础信息即可
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 调用分页查询方法
        // ！！！Page 类是来自 PageHelper 分页插件的类，
        // 它继承自 ArrayList，用于存储当前页的数据列表，
        // 同时包含分页相关的元数据信息。
//        pageNum (int) - 当前页码
//        pageSize (int) - 每页显示的记录数
//        total (long) - 总记录数
//        pages (int) - 总页数
//        size (int) - 当前页的实际记录数
//        ...
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        // 获取总记录数
        long total = page.getTotal();

        // 获取当前页的数据
        List<Employee>  records = page.getResult();

        // 返回结果
        return new PageResult(total,records);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 创建可复用的sql方法，update set status/name/... = ? where id = ?

        // 创建实体类对象（封装状态与id的值）
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();

        // 设置员工状态
        employeeMapper.update(employee);
    }
}
