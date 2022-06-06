package common.cache;

import common.ChannelFutureWrapper;
import common.RpcInvocation;
import common.config.ClientConfig;
import registy.URL;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    //发送消息的队列
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);
    //存放UUid和服务的容器
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
    //provider名称 --> 该服务有哪些集群URL，订阅服务集合
    public static List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    //服务方地址，存放ip
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    public static Map<String, List<ChannelFutureWrapper>> CONNECT_MAP = new ConcurrentHashMap<>();


}
