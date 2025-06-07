package com.cutec.common.error;

import com.cutec.common.enums.CustomError;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.cutec.common.config.UserThreadLocal;
import com.cutec.common.response.Result;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * tip: 如果不加@ResponseBody 不会返回返回体 会返回500
 */
@Slf4j
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<CustomError> handlerException(HttpServletResponse httpServletResponse, Exception ex) {
        // 清除ThreadLocal中的用户信息
        UserThreadLocal.user.remove();

        //  打印异常堆栈信息
//        ex.printStackTrace();

        //  处理异常 处理 http code
        if (ex instanceof CustomizeException cex) {
            httpServletResponse.setStatus(cex.getCustomError().httpCode);
            return new Result<>(cex.getCustomError());
        }

        //  处理异常 提示方法错误
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return new Result<>(CustomError.REQUEST_METHOD_ERROR);
        }

        //  其他异常 返回 500
        httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
        return new Result<>(CustomError.SERVER_ERROR);
    }
}
