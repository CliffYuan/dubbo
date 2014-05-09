/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.proxy.javassist;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.bytecode.Proxy;
import com.alibaba.dubbo.common.bytecode.Wrapper;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.proxy.AbstractProxyFactory;
import com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker;
import com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler;

/**
 * 主要功能：
 * （1）组装Invoker， #该Invoker引用实际的服务提供者
 * （2）组装代理      #该Proxy实现了接口，包装了Invoker,类似其实就是jdk的动态代理
 *
 * 组装Invoker步骤：
 * （1）构造Wrapper对象
 * （2）构造Invoker对象
 * （3）当请求过来时，执行invoker.invoke(Invocation invocation)方法
 *
 * 组装代理步骤：
 * （1）包装Invoker=默认是FailoverClusterInvoker
 *
 * #核心流程#
 *
 * JavaassistRpcProxyFactory 
 * @author william.liangf
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }

    /**
     *
     * @param proxy
     * @param type
     * @param url
     * @param <T>
     * @return
     */
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
        // TODO Wrapper类不能正确处理带$的类名
        logger.xnd("开始创建接口:"+type.getName()+"的包装代理类wrapper");

        //Wrapper字节码后的结果，见下面的注释的invokeMethod（）方法
        final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);

        logger.xnd("结束创建接口:"+type.getName()+"的包装代理类wrapper，通过解析接口中所有方法，根据方法构造成IF语句，然后在执行时，通过传入的类实例调用的对应方法。");

        //执行步骤：
        //a.当请求过来时，执行invoker.invoke(Invocation invocation)方法
        //b.分解Invocation为 执行方法，参数类型和参数
        //c.转而执行invoker.doInvoke()方法
        //d.转而执行Wrapper.invokeMethod()方法   #具体实现见下面注释的invokeMethod（）方法

        Invoker invoker= new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName, 
                                      Class<?>[] parameterTypes, 
                                      Object[] arguments) throws Throwable {
               // logger.xnd("调用Invoker实例执行:"+this.getInterface().getName()+"的"+methodName+"方法");
                logger.xnd("调用Invoker实例转化为执行"+this.getInterface().getSimpleName()+"对应的Wrapper的invokeMethod方法");
                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
        logger.xnd("结束创建接口:"+type.getName()+"的Invoker对象");
        return invoker;
    }


//    public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws java.lang.reflect.InvocationTargetException {
//        com.alibaba.dubbo.demo.DemoService w;
//        try {
//            w = ((com.alibaba.dubbo.demo.DemoService) $1);
//        } catch (Throwable e) {
//            throw new IllegalArgumentException(e);
//        }
//        try {
//            if ("sayHello".equals($2) && $3.length == 1) {
//                return ($w) w.sayHello((java.lang.String) $4[0]);
//            }
//        } catch (Throwable e) {
//            throw new java.lang.reflect.InvocationTargetException(e);
//        }
//        throw new com.alibaba.dubbo.common.bytecode.NoSuchMethodException("Not found method \"" + $2 + "\" in class com.alibaba.dubbo.demo.DemoService.");
//    }

}