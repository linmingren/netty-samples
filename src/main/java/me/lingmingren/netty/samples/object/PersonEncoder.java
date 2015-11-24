package me.lingmingren.netty.samples.object;

import java.io.Serializable;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class PersonEncoder extends ObjectEncoder {

	@Override
	protected void encode(ChannelHandlerContext ctx, Serializable msg,
			ByteBuf out) throws Exception {
		// TODO Auto-generated method stub
		super.encode(ctx, msg, out);
	}

}
