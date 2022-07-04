package router;

import common.ChannelFutureWrapper;
import registy.URL;

/**
 *
 * @author ZGMZC
 * @date 2022/7/4 17:23
 */
public interface IRouter {
    /**
     * 刷新路由数组
     */
    public void refreshRouterArr(Selector selector);

    /**
     * 获取到请求到连接通道
     */
    public ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     * @param url
     */
    public void updateWeight(URL url);
}
