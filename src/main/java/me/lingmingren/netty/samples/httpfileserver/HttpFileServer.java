package me.lingmingren.netty.samples.httpfileserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {

	public static void main(String[] args) throws Exception {
		String ip = "127.0.0.1";
		int port = 8080;

		final HttpStaticFileServerHandler routerHandler = new HttpStaticFileServerHandler();
		ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				//p.addLast("decoder", new HttpRequestDecoder());
				//p.addLast("encoder", new HttpResponseEncoder());
				p.addLast("HttpServerCodec", new HttpServerCodec());
				p.addLast("Aggregator",new HttpObjectAggregator(65536));
				p.addLast("Chunked",new ChunkedWriteHandler());  
				p.addLast(routerHandler);
			}
		};

		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			// public service processor
			ServerBootstrap publicServerBootstrap = new ServerBootstrap();
			publicServerBootstrap.group(bossGroup, workerGroup).channel(
					NioServerSocketChannel.class);
			publicServerBootstrap.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childHandler(channelInitializer);

			// bind to public access host info
			Channel ch1 = publicServerBootstrap.bind(port).sync().channel();

			System.out.println(String.format("Started OK HttpServer at %s:%d",
					ip, port));
			ch1.config().setConnectTimeoutMillis(1800);
			ch1.closeFuture().sync();
			System.out.println("Shutdown...");
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
