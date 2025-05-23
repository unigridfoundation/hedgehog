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

package org.unigrid.hedgehog.model.network.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.unigrid.hedgehog.model.cdi.CDIUtil;
import org.unigrid.hedgehog.model.network.Node;
import org.unigrid.hedgehog.model.network.Topology;
import org.unigrid.hedgehog.model.network.packet.PublishPeers;

@Slf4j
@Sharable
public class PublishPeersChannelHandler extends AbstractInboundHandler<PublishPeers> {
	public PublishPeersChannelHandler() {
		super(PublishPeers.class);
	}

	@Override
	public void typedChannelRead(ChannelHandlerContext context, PublishPeers publishPeers) throws Exception {
		CDIUtil.resolveAndRun(Topology.class, topology -> {
			log.atTrace().log("Received {} peers from {}",
				publishPeers.getNodes().size(), context.channel().remoteAddress()
			);

			for (Node newNode : publishPeers.getNodes()) {
				topology.addNode(newNode);
			}
		});
	}
}
