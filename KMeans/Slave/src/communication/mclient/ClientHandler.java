package communication.mclient;

import communication.utils.Para;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Para> {
    public static volatile ChannelHandlerContext ctx;

    //建立连接时的回调
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }


    //发送消息
    public void sendMsg(Para user){
        ctx.write(user);
    }

    //收到消息时的回调
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Para para) throws Exception {
        System.out.println("receive message from server: " + para.toString());
        MClient.paraQueue.add(para);

    }
}

