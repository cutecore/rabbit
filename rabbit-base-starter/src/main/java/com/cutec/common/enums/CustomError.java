package com.cutec.common.enums;

public enum CustomError {
    // 几种特殊的错误 开始 -----------------------------------
    UNAUTHORIZED(1, 401, "未授权请登录"),
    FORBIDDEN(3, 403, "无权限禁止访问"),
    NOT_FIND(2, 404, "找不到资源"),
    SERVER_ERROR(4, 500, "服务内部错误"),
    // 几种特殊的错误 结束 -----------------------------------


    PASSWORD_ERROR(4, "密码错误"),
    REQUEST_METHOD_ERROR(6, "请求方法错误"),
    DATA_EMPTY(8, "空数据"),
    PROXY_ERROR(10, "无效代理");
    public final Integer code;
    public final Integer httpCode;
    public final String message;

    CustomError(Integer code, String message) {
        this.code = code;
        this.httpCode = 200;
        this.message = message;
    }

    CustomError(Integer code, Integer httpCode, String message) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode;
    }

    @Override
    public String toString() {
        return "Error{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
