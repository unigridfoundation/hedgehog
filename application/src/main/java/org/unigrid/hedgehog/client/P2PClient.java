/*
    Unigrid Hedgehog
    Copyright © 2021-2025 Stiftelsen The Unigrid Foundation

    Stiftelsen The Unigrid Foundation (org. nr: 802482-2408)

    This program is free software: you can redistribute it and/or modify it under the terms of the
    addended GNU Affero General Public License as published by Stiftelsen The Unigrid Foundation and
    the Free Software Foundation, version 3 of the License (see COPYING and COPYING.addendum).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License and the addendum for more details.

    You should have received an addended copy of the GNU Affero General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigridfoundation/hedgehog>.
 */

package org.unigrid.hedgehog.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicClientCodecBuilder;
import io.netty.incubator.codec.quic.QuicSslContext;
import io.netty.incubator.codec.quic.QuicSslContextBuilder;
import io.netty.incubator.codec.quic.QuicStreamType;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import org.unigrid.hedgehog.model.network.handler.PingChannelHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.unigrid.hedgehog.model.Network;
import org.unigrid.hedgehog.model.network.ConnectionContainer;
import org.unigrid.hedgehog.model.network.codec.FrameDecoder;
import org.unigrid.hedgehog.model.network.codec.HelloEncoder;
import org.unigrid.hedgehog.model.network.codec.PingDecoder;
import org.unigrid.hedgehog.model.network.codec.PingEncoder;
import org.unigrid.hedgehog.model.network.codec.PublishPeersDecoder;
import org.unigrid.hedgehog.model.network.codec.PublishPeersEncoder;
import org.unigrid.hedgehog.model.network.codec.PublishSporkDecoder;
import org.unigrid.hedgehog.model.network.codec.PublishSporkEncoder;
import org.unigrid.hedgehog.model.network.handler.PublishPeersChannelHandler;
import org.unigrid.hedgehog.model.network.handler.PublishSporkChannelHandler;
import org.unigrid.hedgehog.model.network.initializer.RegisterQuicChannelInitializer;
import org.unigrid.hedgehog.model.network.schedule.PingSchedule;
import org.unigrid.hedgehog.model.network.schedule.PublishAndSaveSporkSchedule;
import org.unigrid.hedgehog.model.network.schedule.PublishPeersSchedule;

public class P2PClient extends ConnectionContainer {
	public P2PClient(String hostname, int port) throws ExecutionException, InterruptedException, CertificateException,
		NoSuchAlgorithmException, TimeoutException {

		super();
		InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
		group = Optional.of(new NioEventLoopGroup(Network.COMMUNICATION_THREADS));

		final QuicSslContext context = QuicSslContextBuilder.forClient()
			.trustManager(InsecureTrustManagerFactory.INSTANCE)
			.applicationProtocols(Network.getProtocols())
			.build();

		final ChannelHandler codec = new QuicClientCodecBuilder()
			.initialMaxData(Network.MAX_DATA_SIZE)
			.initialMaxStreamDataBidirectionalLocal(Network.MAX_DATA_SIZE)
			.initialMaxStreamDataBidirectionalRemote(Network.MAX_DATA_SIZE)
			.initialMaxStreamsBidirectional(Network.MAX_STREAMS)
			.maxIdleTimeout(Network.IDLE_TIME_MINUTES, TimeUnit.MINUTES)
			.sslContext(context)
			.build();

		final Channel channelBootstrap = new Bootstrap().group(group.get())
			.channel(NioDatagramChannel.class)
			.handler(codec)
			.bind(0).sync().channel();

		QuicChannel quicChannel;

		try {
			quicChannel = QuicChannel.newBootstrap(channelBootstrap)
				.streamHandler(new ChannelInboundHandlerAdapter())
				.remoteAddress(new InetSocketAddress(hostname, port))
				.connect().get(Network.CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);

		} catch (ExecutionException | TimeoutException ex)  {
			group.get().shutdownGracefully();
			throw ex;
		}

		// We create new stream so we can support bidirectional communication (in case we expect a response)
		// TODO: Add support for ChannelCollector
		channel = quicChannel.createStream(QuicStreamType.BIDIRECTIONAL,
			new RegisterQuicChannelInitializer(() -> {
				return Arrays.asList(new LoggingHandler(LogLevel.DEBUG),
					new FrameDecoder(),
					new HelloEncoder(),
					new PingEncoder(), new PingDecoder(),
					new PublishSporkEncoder(), new PublishSporkDecoder(),
					new PublishPeersEncoder(), new PublishPeersDecoder(),
					new PingChannelHandler(), new PublishSporkChannelHandler(),
					new PublishPeersChannelHandler()
				);
			}, () -> {
				return Arrays.asList(
					new PingSchedule(),
					new PublishPeersSchedule(),
					new PublishAndSaveSporkSchedule()
				);
			}, RegisterQuicChannelInitializer.Type.CLIENT)
		).sync().getNow();
	}
}
