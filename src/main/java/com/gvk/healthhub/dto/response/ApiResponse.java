package com.gvk.healthhub.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @JsonIgnore
    private boolean success;
    private String statusMessage;
    private T data;
    private Integer statusCode;
    
    public ApiResponse() {
    }
    
    public ApiResponse(boolean success, String message, T data, Integer statusCode) {
        this.success = success;
        this.statusMessage = message;
        this.data = data;
        this.statusCode = statusCode;
    }
    
    @JsonIgnore
    public boolean isSuccess() {
        return success;
    }
    
    @JsonIgnore
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @JsonIgnore
    public String getMessage() {
        return statusMessage;
    }
    
    @JsonIgnore
    public void setMessage(String message) {
        this.statusMessage = message;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, 200);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, 500);
    }
    
    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return new ApiResponse<>(false, message, null, statusCode);
    }
}