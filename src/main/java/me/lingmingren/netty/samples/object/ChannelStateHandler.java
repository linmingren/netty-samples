package me.lingmingren.netty.samples.object;

import io.netty.channel.ChannelPipeline;

public interface ChannelStateHandler {
	public void actived(ChannelPipeline pipeline);
}
