package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping  // Post请求
    @ApiOperation("新增员工") // 添加接口描述
    public Result save(@RequestBody EmployeeDTO employeeDTO) { // @RequestBody 将post请求的json数据转为EmployeeDTO对象
        log.info("新增员工{}",employeeDTO);
        // 调用service保存员工数据
        employeeService.save(employeeDTO);
        // 返回结果
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */

    @GetMapping("/page") // GET请求，添加接口路径
    @ApiOperation("员工分页查询") // 添加接口描述
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 打印日志
        log.info("员工分页查询，传输为：{}", employeePageQueryDTO);
        // 调用service分页查询
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        // 返回结果
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}") // post请求，添加接口路径，路径参数参数传递status状态，地址栏传输参数传递员工id
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status, Long id) { // @PathVariable注解将路径参数绑定到方法参数中
        // 打印日志
        log.info("启用禁用员工账号：{}，{}", status, id);

        // 调用service方法
        employeeService.startOrStop(status, id);

        // 返回结果
        return Result.success();
    }
}
