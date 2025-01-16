package software.amazon.smithy.java.client.http.netty;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Process {@link io.netty.handler.codec.http.FullHttpResponse} translated from HTTP/2 frames
 */
public class HttpResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final Map<Integer, Entry<ChannelFuture, CompletableFuture<FullHttpResponse>>> streamidPromiseMap;

    public HttpResponseHandler() {
        // Use a concurrent map because we add and iterate from the main thread (just for the purposes of the example),
        // but Netty also does a get on the map when messages are received in a EventLoop thread.
        streamidPromiseMap = PlatformDependent.newConcurrentHashMap();
    }

    /**
     * Create an association between an anticipated response stream id and a {@link io.netty.channel.ChannelPromise}
     *
     * @param streamId The stream for which a response is expected
     * @param writeFuture A future that represent the request write operation
     * @param promise The promise object that will be used to wait/notify events
     * @return The previous object associated with {@code streamId}
     * @see HttpResponseHandler#awaitResponses(long, java.util.concurrent.TimeUnit)
     */
    public Entry<ChannelFuture, CompletableFuture<FullHttpResponse>> put(int streamId, ChannelFuture writeFuture, CompletableFuture<FullHttpResponse> promise) {
        return streamidPromiseMap.put(streamId, new SimpleEntry<>(writeFuture, promise));
    }

    /**
     * Wait (sequentially) for a time duration for each anticipated response
     *
     * @param timeout Value of time to wait for each response
     * @param unit Units associated with {@code timeout}
     * @see HttpResponseHandler#put(int, ChannelFuture, CompletableFuture)
     */
    public void awaitResponses(long timeout, TimeUnit unit) {
        Iterator<Entry<Integer, Entry<ChannelFuture, CompletableFuture<FullHttpResponse>>>> itr = streamidPromiseMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Integer, Entry<ChannelFuture, CompletableFuture<FullHttpResponse>>> entry = itr.next();
            ChannelFuture writeFuture = entry.getValue().getKey();
            if (!writeFuture.awaitUninterruptibly(timeout, unit)) {
                throw new IllegalStateException("Timed out waiting to write for stream id " + entry.getKey());
            }
            if (!writeFuture.isSuccess()) {
                throw new RuntimeException(writeFuture.cause());
            }
            CompletableFuture<FullHttpResponse> promise = entry.getValue().getValue();
            try {
                promise.get(timeout, unit);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }

            System.out.println("---Stream id: " + entry.getKey() + " received---");
            itr.remove();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (streamId == null) {
            System.err.println("HttpResponseHandler unexpected message received: " + msg);
            return;
        }

        Entry<ChannelFuture, CompletableFuture<FullHttpResponse>> entry = streamidPromiseMap.get(streamId);
        if (entry == null) {
            System.err.println("Message received for unknown stream id " + streamId);
        } else {
            entry.getValue().complete(msg);
        }
    }
}