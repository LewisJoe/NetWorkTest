package com.lewis.networktest;

/**
 * Created by Administrator on 15-9-16.
 * 回调机制处理网络线程
 */
public interface HttpCallbackListener {
    //当服务器成功响应我们请求的时候调用
    void onFinish(String response);
    //当网络操作出现错误的时候调用
    void onError(Exception e);
}
