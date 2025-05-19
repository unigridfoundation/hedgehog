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

package org.unigrid.hedgehog.model.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.Optional;
import org.unigrid.hedgehog.model.network.codec.api.PacketEncoder;
import org.unigrid.hedgehog.model.network.packet.Packet;

public abstract class AbstractMessageToByteEncoder<T> extends MessageToByteEncoder<T> implements PacketEncoder<T> {
	private void writeFrameHeader(ChannelHandlerContext ctx, ByteBuf out, int len) throws Exception {
		out.writeShort(FrameDecoder.MAGIC);
		out.writeShort(getCodecType().getValue());
		out.writeInt(len);
	}

	public abstract Optional<ByteBuf> encode(ChannelHandlerContext ctx, T in) throws Exception;

	@Override
	public final void encode(ChannelHandlerContext ctx, T in, ByteBuf out) throws Exception {
		final Optional<ByteBuf> data = encode(ctx, in);

		if (data.isPresent()) {
			writeFrameHeader(ctx, out, data.get().writerIndex());
			out.writeBytes(data.get());
			data.get().release();
		}
	}

	@Override public abstract Packet.Type getCodecType();
}
