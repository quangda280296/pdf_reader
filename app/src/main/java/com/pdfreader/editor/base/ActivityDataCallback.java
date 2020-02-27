package com.pdfreader.editor.base;

public interface ActivityDataCallback<T>{
    void setData(T data);
    T getData();
}