package com.example.myblog.common;

import lombok.Data;

@Data
public class ResultAjax {
    private int code;
    private String msg;
    private Object data;

    public static ResultAjax succ(Object data){
        ResultAjax result = new ResultAjax();
        result.setCode(200);
        result.setMsg("");
        result.setData(data);
        return result;
    }

    public static ResultAjax succ(int code,String msg,Object data){
        ResultAjax result = new ResultAjax();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static ResultAjax succ(int code,String msg){
        ResultAjax result = new ResultAjax();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }


        public static ResultAjax fail(int code,String msg){
        ResultAjax result = new ResultAjax();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static ResultAjax fail(int code,String msg,Object data){
        ResultAjax result = new ResultAjax();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
