package registy;



public interface RegistryService {
    /**
     * 注册接口，当某个服务要启动的时候，需要再将接口注册到注册中心，之后服务调用方才可以获取到新服务的数据了。
     * @param url
     */
    void register(URL url);

    /**
     * 服务下线接口，当某个服务提供者要下线了，则需要主动将注册过的服务信息从zk的指定节点上摘除，此时就需要调用unRegister接口。
     * @param url
     */
    void unRegister(URL url);

    /**
     * 订阅某个服务，通常是客户端在启动阶段需要调用的接口。客户端在启动过程中需要调用该函数，从注册中心中提取现有的服务提供者地址，从而实现服务订阅功能。
     * @param url
     */
    void subscribe(URL url);

    /**
     * 取消订阅服务，当服务调用方不打算再继续订阅某些服务的时候，就需要调用该函数去取消服务的订阅功能，将注册中心的订阅记录进行移除操作。
     * @param url
     */
    void doUnSubscribe(URL url);
}
