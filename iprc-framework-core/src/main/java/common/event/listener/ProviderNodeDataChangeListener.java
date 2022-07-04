package common.event.listener;

import common.ChannelFutureWrapper;
import common.event.IRpcListener;
import common.event.IRpcNodeChangeEvent;
import registy.URL;
import registy.zookeeper.ProviderNodeInfo;

import java.util.List;

import static common.cache.CommonClientCache.CONNECT_MAP;
import static common.cache.CommonClientCache.IROUTER;

/**
 * @author ZGMZC
 * @date 2022/7/4 18:20
 */
public class ProviderNodeDataChangeListener implements IRpcListener<IRpcNodeChangeEvent> {
    @Override
    public void callBack(Object t) {
        ProviderNodeInfo providerNodeInfo = ((ProviderNodeInfo) t);
        List<ChannelFutureWrapper> channelFutureWrappers =  CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort();
            if(address.equals(providerNodeInfo.getAddress())){
                //修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                //更新权重 这里对应了文章顶部的RandomRouterImpl类
                IROUTER.updateWeight(url);
                break;
            }
        }
    }
}
