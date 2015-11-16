package me.lingmingren.netty.samples;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

public class TcpServer {

	static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    public static void main(String[] args) throws Exception {    	       
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 1)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                    // ChannelBuffer cb = ChannelBuffers.
                     //p.addLast(new  DelimiterBasedFrameDecoder(1024,","));
                     p.addLast(new StringDecoder(CharsetUtil.UTF_8));
                     p.addLast(new StringEncoder(CharsetUtil.UTF_8)); 
                     p.addLast(new TcpServerHandler());
                     
                     p.addLast("logger",new MessageToMessageDecoder<String>(){

						@Override
						protected void decode(ChannelHandlerContext ctx,
								String msg, List<Object> out) throws Exception {
							InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
							Map<String, String> request = new HashMap<>();
							request.put("data", msg);
							request.put("from-ip", address.getAddress().getHostAddress());
							out.add(request);
						}
             			     	 
                      });
                     
                     p.addLast("handler",new SimpleChannelInboundHandler<Map<String, String>>(){

						@Override
						protected void messageReceived(
								ChannelHandlerContext ctx,
								Map<String, String> msg) throws Exception {
							// TODO Auto-generated method stub
							System.out.println(String.format("from-host: '%s'", msg.get("from-ip")));
							System.out.println(String.format("data: '%s'", msg.get("data")));
			        		ctx.writeAndFlush("Done");
						}
                    	    
                     });
                     
                 }
             });
            ChannelFuture f = b.bind(PORT).sync();
            Channel channel = f.channel();          
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
