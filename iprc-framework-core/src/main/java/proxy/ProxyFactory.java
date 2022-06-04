package proxy;

public interface ProxyFactory {
    //根据class得到代理对象
    <T> T getProxy(final Class clazz) throws Throwable;
}
