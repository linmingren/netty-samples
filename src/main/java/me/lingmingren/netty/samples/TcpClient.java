package me.lingmingren.netty.samples;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class TcpClient {
	String host;
	int port;
	ChannelPipeline pipeline;
    ChannelHandler clientHandler;
    
  
    
    public TcpClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	protected void execute(){
    	if(clientHandler == null){
    		throw new IllegalArgumentException("clientHandler is NULL, please define a tcpClientChannelHandler !");
    	}
    	
    	// Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             // Configure the connect timeout option.
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     // Decoder
                     p.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));

                     // Encoder
                     p.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));  
                     
                     // the handler for client
                     p.addLast(clientHandler);
                 }
             });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            this.pipeline = f.channel().pipeline();
            
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();            
        } catch (Exception e){   
            e.printStackTrace();
        } finally {
            // Shut down the event loop to terminate all threads.        	
            group.shutdownGracefully();
        }
    }
    
	public void sendMessage(String message) {
		this.pipeline.writeAndFlush(message);
	}
	
	public void close() {
		this.pipeline.close();
	}
	
    public TcpClient buildHandler(String message, ChannelStateHandler asynchCall) throws Exception{
    	clientHandler = new TcpClientHandler(message, asynchCall);
    	return this;
    }        
    
    public static void main(String[] args) throws Exception {    	
    	final TcpClient client = new TcpClient("127.0.0.1",8007).buildHandler("Hello", new ChannelStateHandler() {
			
			public void actived(ChannelPipeline pipeline) {
				System.out.println("Please input your message: ");
		    	Scanner scanner = new Scanner(System.in);
		    	String message;
		    	
		    	while (true) {
		    		message = scanner.next();
		    		
		    		pipeline.writeAndFlush(message);
		    		if (message.equals("bye")) {
		    			pipeline.close();
		    			scanner.close();
		    		}
		    		
		    	}
				
			}
    		
    	}); 
    	
    	client.execute();
    	
    }
}
