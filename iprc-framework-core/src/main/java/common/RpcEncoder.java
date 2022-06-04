package common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol, ByteBuf byteBuf) throws Exception {
        // 魔法数
        byteBuf.writeShort(rpcProtocol.getMagicNumber());
        // 数据长度
        byteBuf.writeInt(rpcProtocol.getContentLength());
        // 数据核心内容
        byteBuf.writeBytes(rpcProtocol.getContent());
    }
}
