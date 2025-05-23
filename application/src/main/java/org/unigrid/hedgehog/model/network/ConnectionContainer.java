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

package org.unigrid.hedgehog.model.network;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.unigrid.hedgehog.model.network.packet.Packet;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionContainer implements Connection {
	@Getter protected QuicStreamChannel channel;
	@Getter protected Optional<EventLoopGroup> group;

	@Override
	public ChannelFuture send(Packet packet) {
		return channel.writeAndFlush(packet);
	}

	@SneakyThrows
	public void close() {
		if (!channel.isShutdown()) {
			channel.shutdown().sync();
		}

		if (group.isPresent()) {
			if (!group.get().isShuttingDown()) {
				group.get().shutdownGracefully().sync();
			}
		}
	}

	@SneakyThrows
	public void closeDirty() {
		if (!channel.isShutdown()) {
			channel.shutdown();
		}

		if (group.isPresent()) {
			if (!group.get().isShuttingDown()) {
				group.get().shutdownGracefully();
			}
		}
	}
}
