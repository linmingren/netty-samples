package me.lingmingren.netty.samples.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author trieu
 * 
 * demo for HeartBeat monitor
 *
 */
public class ImportantServer {
	public static void main(String[] args) throws Exception {
		EventLoopGroup loopGroup = new NioEventLoopGroup();
		try {			
			
			ChannelFuture cf = new Bootstrap().group(loopGroup)
					.channel(NioDatagramChannel.class)
					.option(ChannelOption.SO_BROADCAST, true)
					.handler(new HeartBeatHandler())
					.bind(999);
			
			cf.sync().channel().closeFuture().await();
		} finally {
			loopGroup.shutdownGracefully();
		}
	}
}