package communication.utils;


import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class JsonEncoder extends MessageToByteEncoder<Para> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Para user, ByteBuf buf)
            throws Exception {
        String json = JSON.toJSONString(user);
        ctx.writeAndFlush(Unpooled.wrappedBuffer(json.getBytes()));
    }
}