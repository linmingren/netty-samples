package me.lingmingren.netty.samples.httprouter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Arrays;

@Sharable
public class HttpEventRoutingHandler extends
		SimpleChannelInboundHandler<Object> {

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// TODO Auto-generated method stub
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;

			String uri = request.uri();
			QueryStringDecoder query = new QueryStringDecoder(uri);
			String path = query.path();
			HttpResponse response = null;

			if (uri.startsWith("/hello")) {
				String r = "Hello "
						+ query.parameters()
								.getOrDefault("name", Arrays.asList("guest"))
								.get(0);
				response = buildHttpResponse(r, 200);
			} else {
				response = buildHttpResponse("Oops", 200);
			}

			ctx.write(response);
			ctx.flush().close();
		}
	}

	public HttpResponse buildHttpResponse(String data, int status) {
		ByteBuf byteBuf = Unpooled
				.copiedBuffer(String.valueOf(data).getBytes());
		HttpResponseStatus httpStatus = HttpResponseStatus.valueOf(status);
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1, httpStatus, byteBuf);
		response.headers().set(HttpHeaderNames.CONTENT_TYPE,
				HttpHeaderValues.TEXT_PLAIN);
		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,
				String.valueOf(byteBuf.readableBytes()));
		response.headers().set(HttpHeaderNames.CONNECTION,
				HttpHeaderValues.CLOSE);
		return response;
	}
}