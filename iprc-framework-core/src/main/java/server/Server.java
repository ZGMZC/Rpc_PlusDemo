package server;

import common.RpcDecoder;
import common.RpcEncoder;
import common.config.PropertiesBootstrap;
import common.config.ServerConfig;
import common.utils.CommonUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import registy.RegistryService;
import registy.URL;
import registy.zookeeper.ZookeeperRegister;

import static common.cache.CommonServerCache.PROVIDER_CLASS_MAP;
import static common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * @Auther ZGM
 * @Date created in 16:37 2022/6/3
 */
public class Server {
    private static EventLoopGroup bossGroup=null;
    private static EventLoopGroup workerGroup=null;
    private ServerConfig serverConfig;
    private RegistryService registryService;
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
        this.batchExportUrl();
        bootstrap.bind(serverConfig.getServerPort()).sync();
        System.out.println("开始绑定端口，进行监听和工作...");
    }
    public void initServerConfig() {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
    }
    /**
     * 暴露服务信息
     *
     * @param serviceBean
     */
    public void exportService(Object serviceBean) {
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (registryService == null) {
            registryService = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }
        for(Class interfaceClass:classes){
            PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
            URL url = new URL();
            url.setServiceName(interfaceClass.getName());
            url.setApplicationName(serverConfig.getApplicationName());
            url.addParameter("host", CommonUtils.getIpAddress());
            url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
            PROVIDER_URL_SET.add(url);
        }
    }
    public void batchExportUrl(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (URL url : PROVIDER_URL_SET) {
                    registryService.register(url);
                }
            }
        });
        task.start();
    }
    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.initServerConfig();
        server.exportService(new DataServiceImpl());
        server.startApplication();
    }
}
