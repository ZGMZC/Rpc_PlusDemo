package server;

import common.RpcDecoder;
import common.RpcEncoder;
import common.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import static common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

/**
 * @Auther ZGM
 * @Date created in 16:37 2022/6/3
 */
public class Server {
    private static EventLoopGroup bossGroup=null;
    private static EventLoopGroup workerGroup=null;
    private ServerConfig serverConfig;
    public ServerConfig getServerConfig(){
        return serverConfig;
    }
    public void setServerConfig(ServerConfig serverConfig){
        this.serverConfig=serverConfig;
    }
    public void startApplication() throws InterruptedException{
        bossGroup =new NioEventLoopGroup();
        workerGroup=new NioEventLoopGroup();
        ServerBootstrap bootstrap=new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY,true);
        bootstrap.option(ChannelOption.SO_BACKLOG,1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                System.out.println("初始化Server...");
                socketChannel.pipeline().addLast(new RpcEncoder());
                System.out.println("加入编码器...");
                socketChannel.pipeline().addLast(new RpcDecoder());
                System.out.println("加入译码器...");
                socketChannel.pipeline().addLast(new ServerHandler());
                System.out.println("加入服务端处理器...");
            }
        });
        bootstrap.bind(serverConfig.getPort()).sync();
        System.out.println("开始绑定端口，进行监听和工作...");
    }
    //注册中心，将服务注册到注册中心，简单测试
    public void registyService(Object serviceBean){
        if(serviceBean.getClass().getInterfaces().length==0){
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        //需要注册的对象统一放在一个MAP集合中进行管理
        for(Class c:classes){
            PROVIDER_CLASS_MAP.put(c.getName(), serviceBean);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9090);
        server.setServerConfig(serverConfig);
        server.registyService(new DataServiceImpl());
        server.startApplication();
    }
}
