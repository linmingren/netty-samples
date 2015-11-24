package me.lingmingren.netty.samples.string;

import io.netty.channel.ChannelPipeline;

public interface ChannelStateHandler {
	public void actived(ChannelPipeline pipeline);
}
