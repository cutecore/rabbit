package com.cutec.common.error;

import com.cutec.common.enums.CustomError;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomizeException extends RuntimeException {

    private CustomError customError;

    public CustomizeException(CustomError customError) {
        super(customError.message);
        this.customError = customError;
    }
}
