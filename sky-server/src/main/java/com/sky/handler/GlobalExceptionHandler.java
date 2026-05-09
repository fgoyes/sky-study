package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL中数据重复异常
     * @param ex
     * @return
     */
    @ExceptionHandler // 捕获所有异常
    public Result expectionHandler(SQLIntegrityConstraintViolationException ex){
        // 获取异常信息
        //Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();

        // 判断是否是数据重复异常
        if (message.contains("Duplicate entry")) { // 异常信息是否包含"Duplicate entry"
            String[] split = message.split(" "); // 分割异常信息
            String username = split[2]; // 获取重复的用户名
            String msg = username + "已存在"; // 拼接错误信息
            return Result.error(msg); // 返回错误信息
        } else {
            // MessageConstant.UNKNOWN_ERROR = "未知错误" 常态变量
            return Result.error(MessageConstant.UNKNOWN_ERROR); // 返回未知错误
        }
    }
}
