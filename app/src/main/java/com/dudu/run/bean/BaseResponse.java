package com.dudu.run.bean;
public class BaseResponse<T> {
    private int code;
    private T data;
    private String msg;

    public int a() {
        return this.code;
    }

    public T b() {
        return this.data;
    }

    public String c() {
        return this.msg;
    }
}
