package org.webflux.model;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private T data;
    private List<ApiError> errors;

    // Getters and Setters

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("success");
        response.setCode(HttpStatus.OK.value());
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message, List<ApiError> errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus("error");
        response.setCode(code);
        response.setMessage(message);
        response.setErrors(errors);
        return response;
    }

    public static class ApiError {
        private String field;
        private String message;
    }
}


