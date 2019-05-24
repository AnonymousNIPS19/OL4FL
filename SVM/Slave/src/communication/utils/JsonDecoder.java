package communication.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out)
            throws Exception {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        Para para = JSON.parseObject(new String(bytes, CharsetUtil.UTF_8), Para.class);
        if( null != para )
            out.add(para);
        else
            System.out.println("decpde null");
    }
}
