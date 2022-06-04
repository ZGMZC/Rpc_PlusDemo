package common.cache;

import common.RpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    //发送消息的队列
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue(100);
    //存放UUid和服务的容器
    public static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
}
