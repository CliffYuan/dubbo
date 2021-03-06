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
package com.alibaba.dubbo.rpc.protocol.dubbo;

import java.io.IOException;
import java.io.InputStream;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.io.Bytes;
import com.alibaba.dubbo.common.io.UnsafeByteArrayInputStream;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.codec.ExchangeCodec;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;

import static com.alibaba.dubbo.rpc.protocol.dubbo.CallbackServiceCodec.encodeInvocationArgument;

/**
 * Dubbo codec.
 *
 * (A)两个重要的方法encode（）和decode（）都在ExchangeCodec中
 * (B)encode()根据类别（请求和响应）最后等于执行DubboCodec.encodeRequestData()或者DubboCodec.encodeResponseData()
 * (C)decode()最后等于执行DubboCodec.decodeBody()，然后再根据类别（请求和响应）执行DecodeableRpcInvocation和DecodeableRpcResult的decode()
 *
 *
 *  注意点：
 * （1）是否解码：
 * 根据配置选择是否执行decode（）方法，
 * 如果不是在io中执行，则不调用decode()方法，而在所有的实际执行中都调用下decode()，因为如果执行了则不会执行，有个hasDecoded变量来标示
 *
 * （2）序列化 ObjectOutput ObjectInput
 * ObjectInput.readUTF()=ObjectOutput.writeUTF()
 * ObjectInput.readObject()=ObjectOutput.writeObject()
 * 一一对应，也就是说在序列化时执行对于的write,在反序列化的时候执行对于的read方法。执行方法的顺序是一致的
 *
 *
 * #核心流程#
 *
 * @author qianlei
 * @author chao.liuc
 */
public class DubboCodec extends ExchangeCodec implements Codec2 {

    private static final Logger log = LoggerFactory.getLogger(DubboCodec.class);

    public static final String NAME = "dubbo";

    public static final String DUBBO_VERSION = Version.getVersion(DubboCodec.class, Version.getVersion());

    public static final byte RESPONSE_WITH_EXCEPTION = 0;

    public static final byte RESPONSE_VALUE = 1;

    public static final byte RESPONSE_NULL_VALUE = 2;

    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    /**
     * 读取到的数据的解码
     *
     * 根据是请求和响应对应到DecodeableRpcInvocation和DecodeableRpcResult的decode()
     *
     *
     * @param channel
     * @param is
     * @param header
     * @return
     * @throws IOException
     */
    protected Object decodeBody(Channel channel, InputStream is, byte[] header) throws IOException {
        log.xnd("DubboCodec decodeBody");
        byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
        Serialization s = CodecSupport.getSerialization(channel.getUrl(), proto);//
        log.xnd("DubboCodec decodeBody,序列化对象Serialization="+s);
        // get request id.
        long id = Bytes.bytes2long(header, 4);
        if ((flag & FLAG_REQUEST) == 0) {               //当协议标示不是FLAG_REQUEST时
            // decode response.
            Response res = new Response(id);
            if ((flag & FLAG_EVENT) != 0) {
                res.setEvent(Response.HEARTBEAT_EVENT);
            }
            // get status.
            byte status = header[3];
            res.setStatus(status);
            if (status == Response.OK) {
                try {
                    Object data;
                    if (res.isHeartbeat()) {
                        data = decodeHeartbeatData(channel, deserialize(s, channel.getUrl(), is));
                    } else if (res.isEvent()) {
                        data = decodeEventData(channel, deserialize(s, channel.getUrl(), is));
                    } else {
                        DecodeableRpcResult result;
                        if (channel.getUrl().getParameter(
                            Constants.DECODE_IN_IO_THREAD_KEY,
                            Constants.DEFAULT_DECODE_IN_IO_THREAD)) {
                            log.xnd("DubboCodec decodeBody,解码response,根据url参数decode.in.io，判断在io线程中解码");
                            result = new DecodeableRpcResult(channel, res, is,
                                                             (Invocation)getRequestData(id), proto);
                            result.decode();
                        } else {
                            result = new DecodeableRpcResult(channel, res,
                                                             new UnsafeByteArrayInputStream(readMessageData(is)),
                                                             (Invocation) getRequestData(id), proto);
                        }
                        data = result;
                    }
                    res.setResult(data);
                } catch (Throwable t) {
                    if (log.isWarnEnabled()) {
                        log.warn("Decode response failed: " + t.getMessage(), t);
                    }
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(deserialize(s, channel.getUrl(), is).readUTF());
            }
            return res;
        } else {
            // decode request.
            Request req = new Request(id);
            req.setVersion("2.0.0");
            req.setTwoWay((flag & FLAG_TWOWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                req.setEvent(Request.HEARTBEAT_EVENT);
            }
            try {
                Object data;
                if (req.isHeartbeat()) {
                    data = decodeHeartbeatData(channel, deserialize(s, channel.getUrl(), is));
                } else if (req.isEvent()) {
                    data = decodeEventData(channel, deserialize(s, channel.getUrl(), is));
                } else {
                    DecodeableRpcInvocation inv;
                    if (channel.getUrl().getParameter(
                        Constants.DECODE_IN_IO_THREAD_KEY,
                        Constants.DEFAULT_DECODE_IN_IO_THREAD)) {
                        log.xnd("DubboCodec decodeBody,解码request,根据url参数decode.in.io，判断在io线程中解码");
                        inv = new DecodeableRpcInvocation(channel, req, is, proto);
                        inv.decode();//是否解码，根据配置选择是否执行decode（）方法，用hasDecoded来标示，说明decode()方法可以重复执行。
                    } else {
                        inv = new DecodeableRpcInvocation(channel, req,
                                                          new UnsafeByteArrayInputStream(readMessageData(is)), proto);
                    }
                    data = inv;
                }
                req.setData(data);
            } catch (Throwable t) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode request failed: " + t.getMessage(), t);
                }
                // bad request
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }

    private ObjectInput deserialize(Serialization serialization, URL url, InputStream is)
        throws IOException {
        return serialization.deserialize(url, is);
    }

    private byte[] readMessageData(InputStream is) throws IOException {
        if (is.available() > 0) {
            byte[] result = new byte[is.available()];
            is.read(result);
            return result;
        }
        return new byte[]{};
    }

    /**
     * 请求数据序列化
     *
     * ->DecodeableRpcInvocation.decode()方法
     *
     * 写数据和DecodeableRpcInvocation.decode()方法中的读数据步骤一致
     *
     * @param channel
     * @param out
     * @param data
     * @throws IOException
     */
    @Override
    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        RpcInvocation inv = (RpcInvocation) data;

        out.writeUTF(inv.getAttachment(Constants.DUBBO_VERSION_KEY, DUBBO_VERSION));
        out.writeUTF(inv.getAttachment(Constants.PATH_KEY));
        out.writeUTF(inv.getAttachment(Constants.VERSION_KEY));

        out.writeUTF(inv.getMethodName());
        out.writeUTF(ReflectUtils.getDesc(inv.getParameterTypes()));
        Object[] args = inv.getArguments();
        if (args != null)
        for (int i = 0; i < args.length; i++){
            out.writeObject(encodeInvocationArgument(channel, inv, i));//（1）
        }
        out.writeObject(inv.getAttachments());//步骤（1）也会组张Attachment对象
    }

    /**
     * 响应数据序列化
     *
     * ->对应DecodeableRpcResult.decode()方法
     *
     * 写数据和DecodeableRpcResult.decode()方法中的读数据步骤一致
     *
     * @param channel
     * @param out
     * @param data
     * @throws IOException
     */
    @Override
    protected void encodeResponseData(Channel channel, ObjectOutput out, Object data) throws IOException {
        Result result = (Result) data;

        Throwable th = result.getException();
        if (th == null) {
            Object ret = result.getValue();
            if (ret == null) {
                out.writeByte(RESPONSE_NULL_VALUE);
            } else {
                out.writeByte(RESPONSE_VALUE);
                out.writeObject(ret);
            }
        } else {
            out.writeByte(RESPONSE_WITH_EXCEPTION);
            out.writeObject(th);
        }
    }
}