package client;

import com.alibaba.fastjson.JSON;
import common.RpcDecoder;
import common.RpcEncoder;
import common.RpcInvocation;
import common.RpcProtocol;
import common.config.ClientConfig;
import common.config.PropertiesBootstrap;
import common.event.IRpcListenerLoader;
import common.utils.CommonUtils;
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
import registy.URL;
import registy.zookeeper.AbstractRegister;
import registy.zookeeper.ZookeeperRegister;

import java.util.List;

import static common.cache.CommonClientCache.SEND_QUEUE;
import static common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);
    public static EventLoopGroup clientGroup = new NioEventLoopGroup();
    private ClientConfig clientConfig;
    private IRpcListenerLoader iRpcListenerLoader;
    private Bootstrap bootstrap = new Bootstrap();
    private AbstractRegister abstractRegister;
    public ClientConfig getClientConfig() {
        return clientConfig;
    }
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }
    public RpcReference initClientApplication() throws InterruptedException{
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
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        RpcReference rpcReference;
        rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }
    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean) {
        if (abstractRegister == null) {
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        abstractRegister.subscribe(url);
    }
    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerServiceName, providerIp);
                } catch (InterruptedException e) {

                }
            }
            URL url = new URL();
            url.setServiceName(providerServiceName);
            //客户端在此新增一个订阅的功能
            abstractRegister.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程
     */
    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable {

        public AsyncSendJob() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();
        for (int i = 0; i < 100; i++) {
            try {
//                String result = dataService.sendData("test");
                System.out.println("发送数据："+i+","+i);
                int sum = dataService.sum(i, i);
                System.out.println(sum);
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
