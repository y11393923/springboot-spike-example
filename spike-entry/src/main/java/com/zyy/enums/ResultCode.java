package com.zyy.enums;

public enum ResultCode {
    SUCCESS(true,200,"成功"),
    FAIL(false,500,"服务器开小差"),
    THE_PRODUCT_HAS_BEEN_ROBBED(false,1001,"商品已被抢光"),
    PARAMETER_CANNOT_BE_EMPTY(false, 1002, "参数不能为空"),
    REPEAT_SPIKE(false,1003,"重复秒杀"),
    BEING_QUEUED(false, 1004, "正在排队中"),
    NO_SUCH_ITEM(false, 1005, "没有该商品"),
    SPIKE_HAS_NOT_STARTED(false, 1006,"秒杀未开始"),
    SPIKE_IS_OVER(false, 1007,"秒杀已结束"),
    TOO_MANY_PEOPLE(false, 1008,"活动人数过多，请稍后重试");

    /**
     * 操作是否成功
     */
    boolean success;
    /**
     * 操作代码
     */
    int code;
    /**
     * 提示信息
     */
    String message;

    private ResultCode(boolean success, int code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
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
}
