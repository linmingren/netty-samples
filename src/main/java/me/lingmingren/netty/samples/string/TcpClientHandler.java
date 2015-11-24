package me.lingmingren.netty.samples.string;

import io.netty.channel.ChannelHandler.Skip;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpClientHandler extends SimpleChannelInboundHandler<String> {
	String message;
	ChannelStateHandler asynchCall;
	boolean close = false;

    public TcpClientHandler(String message, ChannelStateHandler asynchCall) {
    	this.message = message;
    	this.asynchCall = asynchCall;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	ctx.writeAndFlush(this.message);
    	System.out.println("channelActive "+ ctx.channel());
    	asynchCall.actived(ctx.pipeline());
    }
    
   
    
    
    @Override
	@Skip
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
    	System.out.println("write "+ ctx.channel());
		super.write(ctx, msg, promise);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
    	System.out.println("channelRead "+ ctx.channel());
		super.channelRead(ctx, msg);
	}

	@Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {    
    	System.out.println("channelRegistered "+ ctx.channel());
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {    	
    	System.out.println("channelUnregistered "+ ctx.channel());
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {    	
    	System.out.println("channelInactive "+ctx.channel());
    }   

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	 ctx.flush();
         
         //close the connection after flushing data to client
    	 if(close){
    		 ctx.close();	 
    	 }  
    	 
    	 System.out.println("channelReadComplete " + ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, String arg1)
			throws Exception {
		System.out.println("messageReceived " + ctx.channel());
	}
}
