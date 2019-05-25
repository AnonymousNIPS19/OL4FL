package communication.mserver;

import communication.utils.Para;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<Para> {
    public static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Para para) throws Exception {
        System.out.println("receive from client: " + para.toString());
        MServer.paraQueue.add( new MParaChannel(para, ctx.channel()));
        System.out.println("receive msg from slaveName: " + para.slaveName + "  queue size:" + MServer.paraQueue.size() );
//        ctx.writeAndFlush("hello client!");
//        ctx.write(user);
//        user.setName("hello client");
//        ctx.writeAndFlush(user);
    }

    // Handle newly added channels
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        for (Channel ch : channelGroup) {
            if (ch == channel) {
                Para user = new Para();

                ch.writeAndFlush(user);
            }
        }
        channelGroup.add(channel);
    }

    public void sendOneChannel(Channel channel, Para para){
        channel.writeAndFlush(para);
    }

    public int broadcast(Para user){
        System.out.println("broadcast...");
        for (Channel ch : channelGroup){
            ch.writeAndFlush(user);
//            ch.write(user);
        }
        return channelGroup.size();
    }

    /**
     * Handle exit message channels
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        for (Channel ch : channelGroup) {
            if (ch == channel) {
                Para user = new Para();
                ch.writeAndFlush(user);
//                ch.writeAndFlush("[" + channel.remoteAddress() + "] leaving");
            }
        }
        channelGroup.remove(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("[" + channel.remoteAddress() + "] leave the room" );
        e.printStackTrace();
        ctx.close().sync();
    }


}
