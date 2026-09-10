package com.amsal.fidmap.apiResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;

    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }


    public static <T> ApiResponse<T> success(String message, T data) {

        return new ApiResponse<>(message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
