package communication.mclient;
import communication.utils.JsonDecoder;
import communication.utils.JsonEncoder;
import communication.utils.Para;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.LinkedBlockingQueue;

public class MClient {
    private String ip = "127.0.0.1";
    private int port = 9991;
    public static volatile ClientHandler clientHandler = new ClientHandler();
    public static volatile Channel serverChannel;
    public static volatile LinkedBlockingQueue<Para> paraQueue = new LinkedBlockingQueue<>();

    public MClient() {
    }
    public MClient(String ip) {
        this.ip = ip;
    }
    public MClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect(String host, int port) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            ch.pipeline().addLast(new JsonDecoder());
                            ch.pipeline().addLast(new JsonEncoder());
                            ch.pipeline().addLast(clientHandler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
//            serverChannel = future.channel();
//            serverChannel.writeAndFlush("now: ");
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void init(MClient mClient) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    mClient.connect(ip, port);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public Para waitForReceive(){
        System.out.println("waitForReceive");
        Para para = null;
        try {
            para = paraQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return para;
    }

//    public static volatile String ip = "127.0.0.1";
//    public static void main(String[] args) throws InterruptedException {
//
////        if( 1 == args.length ){
////            ip = args[0];
////        }
////        System.out.println("ip:" +ip);
////        new Thread(new Runnable() {
////            public void run() {
////                try {
////                    new MClient().connect(ip, 8080);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////            }
////        }).start();
//
//        int i = 100;
//        while(true){
//            Thread.sleep(1000);
//            System.out.println(Thread.currentThread().getState());
//            if( null != clientHandler.ctx){
//                Para user = new Para();
//
//                i++;
//
//                ArrayList<Integer> mylist = new ArrayList<Integer>();
//                for( int j = 10000; j < 20000; j++ ){
//                    mylist.add(j);
//                }
//                System.out.println(mylist.toString());
//                clientHandler.sendMsg(user);
//            }
//            else{
//                System.out.println("null");
//            }
//        }
//    }
}

