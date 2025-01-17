/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.netty;

import static software.amazon.smithy.java.server.netty.NettyUtils.toVoidCompletableFuture;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnixDomainSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.netty.channel.unix.DomainSocketAddress;
import jdk.net.UnixDomainPrincipal;
import software.amazon.smithy.java.logging.InternalLogger;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.core.ErrorHandlingOrchestrator;
import software.amazon.smithy.java.server.core.HandlerAssembler;
import software.amazon.smithy.java.server.core.OrchestratorGroup;
import software.amazon.smithy.java.server.core.ProtocolResolver;
import software.amazon.smithy.java.server.core.SingleThreadOrchestrator;

final class NettyServer implements Server {

    private static final InternalLogger LOG = InternalLogger.getLogger(NettyServer.class);

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final List<URI> endpoints;
    private final OrchestratorGroup orchestrator;

    NettyServer(NettyServerBuilder builder) {
        var bootstrap = new ServerBootstrap();

        var protocolResolver = new ProtocolResolver(builder.serviceMatcher);

        var handlers = new HandlerAssembler().assembleHandlers(builder.serviceMatcher.getAllServices());
        orchestrator = new OrchestratorGroup(
            builder.numberOfWorkers,
            () -> new ErrorHandlingOrchestrator(new SingleThreadOrchestrator(handlers)),
            OrchestratorGroup.Strategy.roundRobin()
        );

        bootstrap.childHandler(new ServerChannelInitializer(orchestrator, protocolResolver));
        int numWorkers = Runtime.getRuntime().availableProcessors() * 2;
        final Function<Integer, EventLoopGroup> eventLoopProvider;
        final ChannelFactory<? extends ServerChannel> channelFactory;
        if (Epoll.isAvailable()) {
            eventLoopProvider = EpollEventLoopGroup::new;
            channelFactory = EpollServerSocketChannel::new;
        } else if (KQueue.isAvailable()) {
            eventLoopProvider = KQueueEventLoopGroup::new;
            // TODO: Make work for both http and uds and...
            channelFactory = KQueueServerDomainSocketChannel::new;
        } else {
            eventLoopProvider = NioEventLoopGroup::new;
            channelFactory = NioServerSocketChannel::new;
        }
        bossGroup = eventLoopProvider.apply(1);
        workerGroup = eventLoopProvider.apply(numWorkers);
        bootstrap.channelFactory(channelFactory);

        this.bootstrap = bootstrap;
        this.endpoints = builder.endpoints;

    }

    @Override
    public void start() {
        for (URI endpoint : endpoints) {
            // TODO: Move somewhere central for both client and server code
            SocketAddress address;
            if (endpoint.getScheme().equals("uds")) {
                // TODO: Need to interpret this relative to a fixed directory,
                // and abstract between windows and *nix pipes
                address = new DomainSocketAddress(endpoint.getHost());
            } else {
                address = new InetSocketAddress(endpoint.getHost(), endpoint.getPort());
            }
            bootstrap.group(bossGroup, workerGroup)
                    .localAddress(address)
                    .bind();
            LOG.info("Started listening on {}", endpoint);
        }
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        return toVoidCompletableFuture(bossGroup.shutdownGracefully())
            .thenCompose(r -> orchestrator.shutdown())
            .thenCompose(r -> toVoidCompletableFuture(workerGroup.shutdownGracefully()));
    }
}
