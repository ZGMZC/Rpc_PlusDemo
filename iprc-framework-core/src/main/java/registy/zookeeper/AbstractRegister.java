package registy.zookeeper;

import registy.RegistryService;
import registy.URL;

import java.util.List;

import static common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * 对一些注册数据做统一的处理，假设日后需要考虑支持多种类型的注册中心，例如redis 、 etcd之类的话，所有基础的记录操作都可以统一放在抽象类里实现。
 */
public abstract class AbstractRegister implements RegistryService {
    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url.getServiceName());
    }

    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 留给子类扩展
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类扩展
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);
    /**
     * 留给子类扩展
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);

}
