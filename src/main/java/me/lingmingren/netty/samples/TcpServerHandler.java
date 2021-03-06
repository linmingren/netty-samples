package me.lingmingren.netty.samples;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;



public class TcpServerHandler extends ChannelHandlerAdapter {

	boolean isCatchedException = false;

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {	
		super.flush(ctx);	
		/*ctx.close().addListener(new GenericFutureListener<Future<? super Void>>() {

			public void operationComplete(Future<? super Void> future)
					throws Exception {
				// TODO Auto-generated method stub
				System.out.println("close connection: "+future.isSuccess());
			}
			
		});*/
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	System.out.println("channelRead");
    	System.out.println(msg);
    	
    	if(msg.equals("")){
    		isCatchedException = true;    		
    		throw new IllegalArgumentException("msg is empty");
    	}
    	
    	// TODO write your logic here
    	StringBuilder s = new StringBuilder();
    	s.append("Ok TCP client, got your message \"").append(msg.toString()).append("\"");
        ctx.write(s.toString());
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {    	
    	super.channelRegistered(ctx);
    	InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
    	System.out.println("channelRegistered "+ address.getAddress());
    	isCatchedException = false;
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {  
    	Thread.sleep(70000);
    	super.channelUnregistered(ctx);
    	InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
    	System.out.println("channelUnregistered "+ address.getAddress());
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {    	
    	super.channelActive(ctx);
    	System.out.println("channelActive "+ctx.channel());
    	ctx.channel().writeAndFlush("connected");
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {    	
    	super.channelInactive(ctx);
    	System.out.println("channelInactive "+ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {    
    	System.out.println("channelReadComplete");
        ctx.flush();
        
        if( ! isCatchedException ){
        	//auto close the client connection after 500 mili-seconds
        	new Timer().schedule(new TimerTask() {			
     			@Override
     			public void run() {
     				ctx.channel().writeAndFlush("close");
     			}
     		}, 500);
        }
       
        
        //close the connection after flushing data to client
        //ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
