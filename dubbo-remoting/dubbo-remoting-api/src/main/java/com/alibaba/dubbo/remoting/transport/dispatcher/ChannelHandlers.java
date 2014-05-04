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
package com.alibaba.dubbo.remoting.transport.dispatcher;


import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Dispatcher;
import com.alibaba.dubbo.remoting.exchange.support.header.HeartbeatHandler;
import com.alibaba.dubbo.remoting.transport.MultiMessageHandler;

/**
 * @author chao.liuc
 *
 */
public class ChannelHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ChannelHandlers.class);

    public static ChannelHandler wrap(ChannelHandler handler, URL url){
        return ChannelHandlers.getInstance().wrapInternal(handler, url);
    }

    protected ChannelHandlers() {}

    protected ChannelHandler wrapInternal(ChannelHandler handler, URL url) {
        ChannelHandler handler1= ExtensionLoader.getExtensionLoader(Dispatcher.class)
                .getAdaptiveExtension().dispatch(handler, url);
        logger.xnd("NettyServer ChannelHandlers.wrapInternal(),根据url中的dispatcher来确定handler处理方式，使用使用线程池,handler="+handler1);
        return new MultiMessageHandler(new HeartbeatHandler(handler1));
    }

    private static ChannelHandlers INSTANCE = new ChannelHandlers();

    protected static ChannelHandlers getInstance() {
        return INSTANCE;
    }

    static void setTestingChannelHandlers(ChannelHandlers instance) {
        INSTANCE = instance;
    }
}