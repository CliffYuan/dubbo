/*
 * Copyright 1999-2011 Alibaba Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;

/**
 * 对所有的message都做一次decode()处理，如果已经decode()则不会在执行。
 * 其实就反序列化
 *
 * @author <a href="mailto:gang.lvg@alibaba-inc.com">kimi</a>
 */
public class DecodeHandler extends AbstractChannelHandlerDelegate {

    private static final Logger log = LoggerFactory.getLogger(DecodeHandler.class);

    public DecodeHandler(ChannelHandler handler) {
        super(handler);
        log.xnd("DecodeHandler 包装 handler,"+handler);
    }

    public void received(Channel channel, Object message) throws RemotingException {
        log.xnd("DecodeHandler received,"+message);
        if (message instanceof Decodeable) {
            decode(message);
        }

        if (message instanceof Request) {
            decode(((Request)message).getData());
        }

        if (message instanceof Response) {
            decode( ((Response)message).getResult());
        }

        handler.received(channel, message);
    }

    private void decode(Object message) {
        log.xnd("DecodeHandler decode,都执行下decode（），已确定该该字节都进行了解码，跟DubboCodec.decodeBody()像对应，"+message);
        if (message != null && message instanceof Decodeable) {
            try {
                ((Decodeable)message).decode();
                if (log.isDebugEnabled()) {
                    log.debug(new StringBuilder(32).append("Decode decodeable message ")
                                  .append(message.getClass().getName()).toString());
                }
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(
                        new StringBuilder(32)
                            .append("Call Decodeable.decode failed: ")
                            .append(e.getMessage()).toString(),
                        e);
                }
            } // ~ end of catch
        } // ~ end of if
    } // ~ end of method decode

}
