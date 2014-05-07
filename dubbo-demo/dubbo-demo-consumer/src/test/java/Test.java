/**
 * Created by yuanyuanming on 14-5-7.
 */
public class Test {
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




//
//    public void destroy() {throw new UnsupportedOperationException("method public abstract void com.alibaba.dubbo.rpc.Protocol.destroy() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
//    }
//    public int getDefaultPort() {throw new UnsupportedOperationException("method public abstract int com.alibaba.dubbo.rpc.Protocol.getDefaultPort() of interface com.alibaba.dubbo.rpc.Protocol is not adaptive method!");
//    }
//    public com.alibaba.dubbo.rpc.Exporter export(com.alibaba.dubbo.rpc.Invoker arg0) throws com.alibaba.dubbo.rpc.Invoker {
//        if (arg0 == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument == null");
//        if (arg0.getUrl() == null) throw new IllegalArgumentException("com.alibaba.dubbo.rpc.Invoker argument getUrl() == null");com.alibaba.dubbo.common.URL url = arg0.getUrl();
//        String extName = ( url.getProtocol() == null ? "dubbo" : url.getProtocol() );
//        if(extName == null) throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
//        com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol)ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension(extName);
//        return extension.export(arg0);
//    }
//    public com.alibaba.dubbo.rpc.Invoker refer(java.lang.Class arg0, com.alibaba.dubbo.common.URL arg1) throws java.lang.Class {
//        if (arg1 == null) throw new IllegalArgumentException("url == null");
//        com.alibaba.dubbo.common.URL url = arg1;
//        String extName = ( url.getProtocol() == null ? "dubbo" : url.getProtocol() );
//        if(extName == null) throw new IllegalStateException("Fail to get extension(com.alibaba.dubbo.rpc.Protocol) name from url(" + url.toString() + ") use keys([protocol])");
//        com.alibaba.dubbo.rpc.Protocol extension = (com.alibaba.dubbo.rpc.Protocol)ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension(extName);
//        return extension.refer(arg0, arg1);
//    }
}
