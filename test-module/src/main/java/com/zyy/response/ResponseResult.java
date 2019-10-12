package com.zyy.response;

/**
 * @Author:zhouyuyang
 * @Description:
 * @Date: Created in 15:39 2019/5/27
 */
public class ResponseResult {
    /**
     * 操作是否成功
     */
    private boolean success;
    /**
     * 操作代码
     */
    private int code;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 数据
     */
    private Object data;

    public ResponseResult(){
    }

    public ResponseResult(int code, String message){
        this(code,message,null);
    }
    public ResponseResult(int code, String message, Object data){
        this.code=code;
        this.message=message;
        this.data=data;
    }
    public ResponseResult(int code, String message, boolean success){
        this(code,message,success,null);
    }
    public ResponseResult(int code, String message, boolean success, Object data){
        this.code=code;
        this.message=message;
        this.success=success;
        this.data=data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseResult{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
