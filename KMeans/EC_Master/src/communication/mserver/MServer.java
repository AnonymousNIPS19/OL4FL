package communication.mserver;

import communication.utils.JsonDecoder;
import communication.utils.JsonEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.LinkedBlockingQueue;

public class MServer {
    public static volatile LinkedBlockingQueue<MParaChannel> paraQueue = new LinkedBlockingQueue<>();
    public static volatile ServerHandler serverHandler =  new ServerHandler();
    private static volatile int PORT = 8802;
    private static volatile EventLoopGroup bossGroup;
    private static volatile EventLoopGroup workerGroup;
    public MServer() {
        this.PORT = 8802;
    }
    public MServer(int port) {
        this.PORT = port;
    }

    public void bind(int port) throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 这里将LengthFieldBasedFrameDecoder添加到pipeline的首位，因为其需要对接收到的数据
                            // 进行长度字段解码，这里也会对数据进行粘包和拆包处理
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            // 对经过粘包和拆包处理之后的数据进行json反序列化，从而得到User对象
                            ch.pipeline().addLast(new JsonDecoder());
                            // LengthFieldPrepender是一个编码器，主要是在响应字节数据前面添加字节长度字段
                            ch.pipeline().addLast(new LengthFieldPrepender(4));
                            // 对响应数据进行编码，主要是将User对象序列化为json
                            ch.pipeline().addLast(new JsonEncoder());

                            // 处理客户端的请求的数据，并且进行响应
                            ch.pipeline().addLast(serverHandler );
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            if ( null != bossGroup )
                bossGroup.shutdownGracefully();
            if ( null != workerGroup )
                workerGroup.shutdownGracefully();
            System.out.println("连接已关闭!");
        }
    }

    public static void closeGracefully(){
        if ( null != bossGroup )
            bossGroup.shutdownGracefully();
        if ( null != workerGroup )
            workerGroup.shutdownGracefully();
    }

    public void begin(){
        try {
            this.bind(PORT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    new MServer().bind(8080);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        int i = 0;
//        ArrayList<Integer> mylist = new ArrayList<Integer>();
//        for( int j = 0; j < 10000; j++ ){
//            mylist.add(j);
//        }
//        User user = new User();
//        user.setMylist(mylist);
//        System.out.println(mylist.toString());
//        while(true){
//            Thread.sleep(3000);
//            user.setName("broadcast");
//            user.setAge(i);
//            i++;
//            serverHandler.broadcast(user);
//        }

}