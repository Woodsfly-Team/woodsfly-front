package com.example.woodsfly.http;
/**
 * 基础API响应结果类，用于封装API调用的结果。
 *
 * @param <T> 泛型类型，用于存储具体的数据对象。
 * @author zoeyyyy-git
 * @Time 2024-8-28
 */
public class BaseApiResult<T> {

    int status;
    int code;
    String message;
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
