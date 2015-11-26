package me.lingmingren.netty.samples.httpdownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Skip;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

public class HttpDownloader {

	public static class HttpDownloadHandler extends
			SimpleChannelInboundHandler<HttpObject> {
		int written = 0;
		File file;
		FileOutputStream outStream;
		FileChannel fileChnl;

		public HttpDownloadHandler(File file) {
			super();
			this.file = file;
		}

		void initFileChannel() throws FileNotFoundException {
			outStream = new FileOutputStream(file);
			fileChnl = outStream.getChannel();
		}

		void writeBytesToFile(ByteBuf byteBuf) throws IOException {
			int writtenIndex = 0;
			try {
				ByteBuffer byteBuffer = byteBuf.nioBuffer();
				writtenIndex += fileChnl.write(byteBuffer, written);
				written += writtenIndex;
			//	byteBuf.readerIndex(byteBuf.readerIndex() + writtenIndex);
				fileChnl.force(false);
			} catch (Exception e) {
				fileChnl.close();
				outStream.close();
			}
		}

		//16K for a message, for a 17K message, channelRead invoked twice
		@Override
		protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) {
			
			try {
				if (msg instanceof HttpResponse) {
					System.out.println("received HttpResponse:");
					initFileChannel();
				} else if (msg instanceof HttpContent) {
					System.out.println("received HttpContent:");
					if (fileChnl == null) {
						initFileChannel();
					}
					ByteBuf byteBuf = ((HttpContent) msg).content();
					writeBytesToFile(byteBuf);
					
					if (msg instanceof LastHttpContent) {
						System.out.println("received LastHttpContent:");
						if (fileChnl != null && outStream != null) {
							fileChnl.close();
							outStream.close();
						}
						ctx.close();
					}
				} 
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			System.out.println("channelRead");
			super.channelRead(ctx, msg);
		}

		@Override
		@Skip
		public void channelReadComplete(ChannelHandlerContext ctx)
				throws Exception {
			// TODO Auto-generated method stub
			System.out.println("channelReadComplete");
			super.channelReadComplete(ctx);
		}
		
		
	}

	public static void main(String[] args) throws Exception {
		//https://codeload.github.com/trieu/netty-cookbook/zip/master
		//http://mirrors.yun-idc.com/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-1503-01.iso
		String url = "http://mirrors.yun-idc.com/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-1503-01.iso";
		File file = new File("./test.html");
		final ChannelHandler handler = new HttpDownloadHandler(file);
		
		URI uri = new URI(url);
		String host =  uri.getHost();
		int port = 80;
		
		
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch)
						
								throws Exception {
							 ChannelPipeline p = ch.pipeline(); 
							 p.addLast(new HttpClientCodec());
						        p.addLast(new HttpContentDecompressor());       
						        p.addLast(handler);
							
						}
						
					});
			// Make the connection attempt.
			Channel ch = b.connect(host, port).sync().channel();
			//ch.config().setAllocator(allocator);
			// Prepare the HTTP request.
			HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
			HttpHeaders headers = request.headers();
			headers.set(HttpHeaderNames.HOST, host);
			//headers.set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);
			headers.set(HttpHeaderNames.ACCEPT_ENCODING,HttpHeaderValues.GZIP);
			// Set some example cookies.
			headers.set(HttpHeaderNames.COOKIE, ClientCookieEncoder.encode(new DefaultCookie("my-cookie", "foo")));
			ch.writeAndFlush(request);
			// Wait for the server to close the connection.
			ch.closeFuture().sync();
			Thread.sleep(100000);
		} finally {
			// Shut down executor threads to exit.
			group.shutdownGracefully();
		}	
	}

}
