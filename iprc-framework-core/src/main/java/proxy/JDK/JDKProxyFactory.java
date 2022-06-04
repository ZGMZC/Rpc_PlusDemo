package proxy.JDK;

import proxy.ProxyFactory;

import java.lang.reflect.Proxy;

public class JDKProxyFactory implements ProxyFactory {

    //代理工厂，内部是JDKClientInvocationHandler
    @Override
    public <T> T getProxy(final Class clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new JDKClientInvocationHandler(clazz));
    }

}
