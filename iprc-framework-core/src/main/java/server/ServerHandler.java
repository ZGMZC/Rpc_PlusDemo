package server;

import com.alibaba.fastjson.JSON;
import common.RpcInvocation;
import common.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

import static common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    //服务端接收到的RpcProtocol协议，反序列化得到RpcInvocation，通过RpcInvocation中的类名、方法名寻找服务，并使用
    //参数，调用该服务， 得到返回结果（若有），将返回结果赋值给RpcInvocation（若有），将其序列化成RpcProtocol，返回给客户端
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //服务端接收数据的时候统一以RpcProtocol协议的格式接收，具体的发送逻辑见客户端发送部分
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        //这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合，通过类名来找到Bean集合中的类
        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        //得到匹配类的所有方法
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;
        //遍历所有方法
        for (Method method : methods) {
            //如果方法名和请求的目标方法相匹配，就运行该方法，参数为类对象、参数列表
            if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                //如果方法返回值为空，就没有返回值;不为空，就将返回结果赋给result
                if (method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(aimObject, rpcInvocation.getArgs());
                } else {
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
                break;
            }
        }
        //将返回结果注入到Invocation中
        rpcInvocation.setResponse(result);
        //把Invocation中的值序列化，得到响应结果的RpcProtocol
        RpcProtocol respRpcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());
        //将响应结果返回
        ctx.writeAndFlush(respRpcProtocol);
    }

    //异常处理，出现异常后，若通道活跃则关闭通道。
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
