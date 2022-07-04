package common.config;

public class ClientConfig {
    private String applicationName;

    private String registerAddr;

    private String proxyType;

    private String RouterStrategy;

    public String getRouterStrategy() {
        return RouterStrategy;
    }

    public void setRouterStrategy(String RouterStrategy) {
        this.RouterStrategy = RouterStrategy;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
}
