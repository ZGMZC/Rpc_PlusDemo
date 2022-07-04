package common;

import java.util.concurrent.atomic.AtomicLong;

import static common.cache.CommonClientCache.SERVICE_ROUTER_MAP;

/**
 * @author ZGMZC
 * @date 2022/7/4 18:11
 */
public class ChannelFutureRefWrapper {
    private AtomicLong referenceTimes=new AtomicLong(0);
    public ChannelFutureWrapper getChannelFutureWrapper(String serviceName){
        ChannelFutureWrapper[] arr = SERVICE_ROUTER_MAP.get(serviceName);
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }
}
