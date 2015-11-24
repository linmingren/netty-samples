package me.lingmingren.netty.samples.object;

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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

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
             .option(ChannelOption.SO_TIMEOUT, 30000)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
            // .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
             .handler(new LoggingHandler(LogLevel.DEBUG))
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     // Decoder
                     p.addLast("PersonDecoder", new PersonDecoder());

                     // Encoder
                     p.addLast("PersonEncoder", new PersonEncoder());  
                  //   p.addLast("readTimeoutHandler", new ReadTimeoutHandler(30));
                 //    p.addLast("writeTimeoutHandler", new WriteTimeoutHandler(30));
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
		    		
		    		StringBuffer sb = new StringBuffer();
		    		int count = 0;
		    		while ( ++count < 2) {
		    			sb.append(message);
		    		}
		    		
		    		count = 0;
		    	//	pipeline.addl
		    		pipeline.channel().isWritable();
		    		
		    		ChannelFuture f = pipeline.writeAndFlush(new Person(message, System.currentTimeMillis()));
		    		f.addListeners(new GenericFutureListener<Future<? super Void>>() {

		    			public void operationComplete(Future<? super Void> future)
		    					throws Exception {
		    				// TODO Auto-generated method stub
		    				System.out.println("operation complete: "+future.get());
		    			}
		    			
		    		});
		    		
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
