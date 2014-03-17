package com.alibaba.dubbo.demo.provider;

import com.alibaba.dubbo.demo.XndService;
import com.alibaba.dubbo.rpc.RpcContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cliffyuan on 14-3-17.
 */
public class XndServiceImpl implements XndService {
    public String sayHello(String name) {
        System.out.println("2-[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] Hello " + name + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        return "2-Hello " + name + ", response form provider: " + RpcContext.getContext().getLocalAddress();
    }
}
