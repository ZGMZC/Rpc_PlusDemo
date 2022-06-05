package common;

import io.netty.channel.ChannelFuture;

/**
 * 服务注册层的功能开发
 */
public class ChannelFutureWrapper {
    private ChannelFuture channelFuture;
    private String host;
    private Integer port;

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
