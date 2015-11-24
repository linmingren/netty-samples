package me.lingmingren.netty.samples.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Date;


public class ServerHealthChecker {
	static final int PORT = 8080;

	public static void main(String[] args) throws Exception {
		SimpleChannelInboundHandler<DatagramPacket> handler = new SimpleChannelInboundHandler<DatagramPacket>() {
			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
				cause.printStackTrace();
				ctx.close();
			}
			
			
			@Override
			protected void messageReceived(ChannelHandlerContext ctx,
					DatagramPacket msg) throws Exception {
				// TODO Auto-generated method stub
				String response = msg.content().toString(CharsetUtil.UTF_8);
				System.out.println("LogClient: " + response);
				ctx.close();
			}


		};
		EventLoopGroup loopGroup = new NioEventLoopGroup();
		try {
			ChannelFuture cf = new Bootstrap().group(loopGroup)
					.channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.handler(new HeartBeatHandler())
					.bind(0);
			Channel ch = cf.sync().channel();
			// Broadcast to port 8080
			InetSocketAddress udpAddr =  new InetSocketAddress("255.255.255.255", 999);
			for (int i=0;i<5;i++) {
				ByteBuf buf = Unpooled.copiedBuffer("ping at " + new Date(),CharsetUtil.UTF_8);
				ch.write(new DatagramPacket(buf,udpAddr));
				Thread.sleep(1000);
			}
			ch.flush().closeFuture().sync();
			//If the channel is not closed within 5 seconds, quit			
			if (!ch.closeFuture().await(10000)) {
				System.err.println("request timed out.");
			}
		} finally {
			loopGroup.shutdownGracefully();
		}
	}
}
