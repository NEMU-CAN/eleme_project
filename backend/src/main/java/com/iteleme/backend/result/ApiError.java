package com.iteleme.backend.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private Integer code;
    private String message;
    private List<ErrorDetail> details;

    public ApiError(Integer code, String message) {
        this(code, message, null);
    }
}
