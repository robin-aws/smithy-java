package software.amazon.smithy.java.client.http.netty;


import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.SslContext;

import java.net.InetSocketAddress;

import static io.netty.handler.logging.LogLevel.INFO;

/**
 */
public class HttpClientInitializer extends ChannelInitializer<Channel> {
    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, HttpClientInitializer.class);

    private final SslContext sslCtx;
    private HttpResponseHandler responseHandler;

    public HttpClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(Channel ch) throws Exception {
        responseHandler = new HttpResponseHandler();
        if (sslCtx != null) {
            throw new UnsupportedOperationException();
        } else {
            configureClearText(ch);
        }
    }

    public HttpResponseHandler responseHandler() {
        return responseHandler;
    }

    /**
     * Configure the pipeline for cleartext.
     */
    private void configureClearText(Channel ch) {
        HttpClientCodec sourceCodec = new HttpClientCodec();
        ch.pipeline().addLast(sourceCodec,
                new HttpObjectAggregator(1048576),
                responseHandler);
    }
}