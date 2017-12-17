package com.bigdata.netty;

import com.bigdata.serviceEcho.EchoInHandler1;
import com.bigdata.serviceEcho.EchoInHandler2;
import com.bigdata.serviceEcho.EchoOutHandler1;
import com.bigdata.serviceEcho.EchoOutHandler2;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Service {
    private final int port;

    public Service(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup eventLoopGroup = null;
        try {
            //server端引导类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //连接池处理数据
            eventLoopGroup = new NioEventLoopGroup();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)//指定通道类型为NioServerSocketChannel，一种异步模式，OIO阻塞模式为OioServerSocketChannel
                    .localAddress("localhost",port)//设置InetSocketAddress让服务器监听某个端口已等待客户端连接。
                    .childHandler(new ChannelInitializer<Channel>() {//设置childHandler执行所有的连接请求
                        protected void initChannel(Channel ch) throws Exception {
                            // 注册两个InboundHandler，执行顺序为注册顺序，所以应该是InboundHandler1 InboundHandler2
                            // 注册两个OutboundHandler，执行顺序为注册顺序的逆序，所以应该是OutboundHandler2 OutboundHandler1
                            ch.pipeline().addLast(new EchoInHandler1());
                            ch.pipeline().addLast(new EchoOutHandler1());
                            ch.pipeline().addLast(new EchoOutHandler2());
                            ch.pipeline().addLast(new EchoInHandler2());

                        }
                    });
            // 最后绑定服务器等待直到绑定完成，调用sync()方法会阻塞直到服务器完成绑定,然后服务器等待通道关闭，因为使用sync()，所以关闭操作也会被阻塞。
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println("开始监听，端口为：" + channelFuture.channel().localAddress());
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        new Service(8899).start();
    }
}
