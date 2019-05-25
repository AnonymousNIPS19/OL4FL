package communication.mclient;

import communication.utils.Para;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Para> {
    public static volatile ChannelHandlerContext ctx;

    // Callback when a connection is established
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }


    // Send message
    public void sendMsg(Para user){
        ctx.write(user);
    }

    // Callback when a message is received
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Para para) throws Exception {
        System.out.println("receive message from server: " + para.toString());
        MClient.paraQueue.add(para);

    }
}

