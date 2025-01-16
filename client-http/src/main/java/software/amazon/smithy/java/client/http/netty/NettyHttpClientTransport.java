/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.http.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.HttpConversionUtil;
import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.http.api.HttpRequest;
import software.amazon.smithy.java.http.api.HttpResponse;
import software.amazon.smithy.java.http.api.ModifiableHttpHeaders;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.logging.InternalLogger;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;

/**
 * A client transport that uses Netty to send {@link HttpRequest} and return
 * {@link HttpResponse}.
 */
public class NettyHttpClientTransport implements ClientTransport<HttpRequest, HttpResponse> {

    private static final InternalLogger LOGGER = InternalLogger.getLogger(NettyHttpClientTransport.class);

    public NettyHttpClientTransport() {}

    @Override
    public Class<HttpRequest> requestClass() {
        return HttpRequest.class;
    }

    @Override
    public Class<HttpResponse> responseClass() {
        return HttpResponse.class;
    }

    @Override
    public CompletableFuture<HttpResponse> send(Context context, HttpRequest request) {
        return sendRequest(createNettyRequest(context, request), request.uri());
    }

    private FullHttpRequest createNettyRequest(Context context, HttpRequest request) {
        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                smithyToNettyMethod(request.method()),
                request.uri().toString(),
                // TODO: streaming
                wrappedBuffer(request.body().waitForByteBuffer()));
        nettyRequest.headers().add(HttpHeaderNames.HOST, request.uri().getHost());
        nettyRequest.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text().toString(), List.of(request.uri().getScheme()));
        if (request.body().contentType() != null) {
            nettyRequest.headers().add(HttpHeaderNames.CONTENT_TYPE, request.body().contentType());
        }
        nettyRequest.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        nettyRequest.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);

        // TODO:
//        Duration requestTimeout = context.get(HttpContext.HTTP_REQUEST_TIMEOUT);
//
//        if (requestTimeout != null) {
//            httpRequestBuilder.timeout(requestTimeout);
//        }

        return nettyRequest;
    }

    private CompletableFuture<HttpResponse> sendRequest(FullHttpRequest request, URI endpoint) {
        // TODO: use GenericFutureListener to chain async results instead
        // TODO: cache connections per endpoint (look at how Java client works)

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        HttpClientInitializer initializer = new HttpClientInitializer(null, Integer.MAX_VALUE);

        // Configure the client.
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.remoteAddress(endpoint.getHost(), endpoint.getPort());
        b.handler(initializer);

        // Start the client.
        Channel channel = b.connect().syncUninterruptibly().channel();

        HttpResponseHandler responseHandler = initializer.responseHandler();
        int streamId = 3;
        System.err.println("Sending request(s)...");

        CompletableFuture<FullHttpResponse> responseFuture = new CompletableFuture<>();
        responseHandler.put(streamId, channel.write(request), responseFuture);
        channel.flush();
        responseHandler.awaitResponses(5, TimeUnit.SECONDS);

        System.out.println("Finished request(s)");

        try {
            return CompletableFuture.completedFuture(createSmithyResponse(responseFuture.get()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            // Wait until the connection is closed.
            channel.close().syncUninterruptibly();
        }
    }

    private HttpResponse createSmithyResponse(FullHttpResponse response) {
        LOGGER.trace("Got response: {}; headers: {}", response, response.headers());

        return HttpResponse.builder()
            .httpVersion(software.amazon.smithy.java.http.api.HttpVersion.HTTP_1_1)
            .statusCode(response.status().code())
            .headers(nettyToSmithyHeaders(response.headers()))
            .body(responseBodyAsDataStream(response))
            .build();
    }

    private static HttpHeaders nettyToSmithyHeaders(io.netty.handler.codec.http.HttpHeaders headers) {
        ModifiableHttpHeaders smithyHeaders = HttpHeaders.ofModifiable();
        for (Map.Entry<String, String> entry : headers) {
            smithyHeaders.putHeader(entry.getKey(), entry.getValue());
        }
        return smithyHeaders;

    }

    private static HttpMethod smithyToNettyMethod(String method) {
        return switch (method) {
            case "GET" -> GET;
            case "POST" -> POST;
            // TODO: complete
            default -> throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        };
    }

    private DataStream responseBodyAsDataStream(FullHttpResponse response) {
        ByteBuf content = response.content();
        int contentLength = content.readableBytes();
        byte[] arr = new byte[contentLength];
        content.readBytes(arr);

        return DataStream.ofByteBuffer(ByteBuffer.wrap(arr), response.headers().get(HttpHeaderNames.CONTENT_TYPE));
    }

    public static final class Factory implements ClientTransportFactory<HttpRequest, HttpResponse> {

        @Override
        public String name() {
            return "http-netty";
        }

        // TODO: Determine what configuration is actually needed.
        @Override
        public NettyHttpClientTransport createTransport(Document node) {
            return new NettyHttpClientTransport();
        }

        @Override
        public Class<HttpRequest> requestClass() {
            return HttpRequest.class;
        }

        @Override
        public Class<HttpResponse> responseClass() {
            return HttpResponse.class;
        }
    }
}
