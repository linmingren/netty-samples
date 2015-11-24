package me.lingmingren.netty.samples.object;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

public class PersonDecoder extends ObjectDecoder{

	public PersonDecoder() {
		super(ClassResolvers.weakCachingConcurrentResolver(null));
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in)
			throws Exception {
		// TODO Auto-generated method stub
		return super.decode(ctx, in);
	}

	
}
