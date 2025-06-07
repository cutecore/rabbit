package com.cutec.common.response;


import lombok.Data;
import com.cutec.common.enums.CustomError;
import java.io.Serializable;
import java.util.Date;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private T data;
    private String message;
    private Date time;

    public Result(T data) {
        if (data instanceof CustomError customError) {
            this.setCode(customError.code);
            this.setData(null);
            this.setMessage(customError.message);
        } else {
            this.setCode(0);
            this.setData(data);
            this.setMessage("happy.");
        }
        this.time = new Date();
    }
}
