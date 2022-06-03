package common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol rpcProtocol, ByteBuf byteBuf) throws Exception {
        byteBuf.writeShort(rpcProtocol.getMagicNumber());
        byteBuf.writeInt(rpcProtocol.getContentLength());
        byteBuf.writeBytes(rpcProtocol.getContent());
    }
}
