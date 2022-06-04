package client;

import com.alibaba.fastjson.JSON;
import common.RpcDecoder;
import common.RpcEncoder;
import common.RpcInvocation;
import common.RpcProtocol;
import common.config.ClientConfig;
import interfaces.DataService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proxy.JDK.JDKProxyFactory;

import static common.cache.CommonClientCache.SEND_QUEUE;

public class Client {
    private ClientConfig clientConfig;
    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }
    public RpcReference startClientApplication() throws InterruptedException{
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //管道中初始化一些逻辑，这里包含了上边所说的编解码器和客户端响应类
                System.out.println("初始化Client...");
                ch.pipeline().addLast(new RpcEncoder());
                System.out.println("加入编码器...");
                ch.pipeline().addLast(new RpcDecoder());
                System.out.println("加入译码器...");
                ch.pipeline().addLast(new ClientHandler());
                System.out.println("加入客户端处理器...");
            }
        });
        //常规的链接netty服务端
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
        this.startClient(channelFuture);
        //这里注入了一个代理工厂
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }
    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable{
        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    //将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端，这里正好对应了Server端的ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    //netty的通道负责发送数据给服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);
        RpcReference rpcReference = client.startClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        for(int i=0;i<100;i++){
            System.out.println("发送数据："+i+"+"+i);
            int res=dataService.sum(i,i);
            System.out.println(res);
            Thread.sleep(1000);
        }
    }
}
