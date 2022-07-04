package common;

import io.netty.channel.ChannelFuture;

/**
 * 服务注册层的功能开发
 */
public class ChannelFutureWrapper {
    private ChannelFuture channelFuture;
    private String host;
    private Integer port;
    private Integer weight;

    @Override
    public String toString() {
        return "ChannelFutureWrapper{" +
                "channelFuture=" + channelFuture +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                '}';
    }

    public ChannelFutureWrapper() {
    }

    public ChannelFutureWrapper(String host, Integer port) {
        this(host,port,0);
    }

    public ChannelFutureWrapper(String host, Integer port, Integer weight) {
        this.host = host;
        this.port = port;
        this.weight = weight;
    }

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

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
