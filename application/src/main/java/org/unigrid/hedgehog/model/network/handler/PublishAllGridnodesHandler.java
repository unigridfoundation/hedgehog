/*
    Unigrid Hedgehog
    Copyright © 2021-2023 Stiftelsen The Unigrid Foundation, UGD Software AB

    Stiftelsen The Unigrid Foundation (org. nr: 802482-2408)
    UGD Software AB (org. nr: 559339-5824)

    This program is free software: you can redistribute it and/or modify it under the terms of the
    addended GNU Affero General Public License as published by the The Unigrid Foundation and
    the Free Software Foundation, version 3 of the License (see COPYING and COPYING.addendum).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License and the addendum for more details.

    You should have received an addended copy of the GNU Affero General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/hedgehog>.
 */

package org.unigrid.hedgehog.model.network.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.unigrid.hedgehog.command.option.GridnodeOptions;
import org.unigrid.hedgehog.model.cdi.CDIUtil;
import org.unigrid.hedgehog.model.gridnode.Gridnode;
import org.unigrid.hedgehog.model.network.Topology;
import static org.unigrid.hedgehog.model.network.handler.ConnectionHandler.SOCKET_ADDRESS_KEY;
import org.unigrid.hedgehog.model.network.packet.PublishAllGridnodes;

@Slf4j
@Sharable
public class PublishAllGridnodesHandler extends AbstractInboundHandler<PublishAllGridnodes> {

	public PublishAllGridnodesHandler() {
		super(PublishAllGridnodes.class);
		log.atDebug().log("Init");
	}

	//TODO:fix duplicate code that is in PublishGridnodeChannelHandler
	@Override
	public void typedChannelRead(ChannelHandlerContext ctx, PublishAllGridnodes obj) throws Exception {
		CDIUtil.resolveAndRun(Topology.class, topology -> {

			Set<Gridnode> gridnodes = topology.cloneGridnode();
			boolean isEmpty = true;
			//System.out.println(gridnodes.size());

			for (Gridnode gridnode : obj.getGridnodes()) {
				if (gridnode.getHostName().contains("0.0.0.0")
					&& !gridnode.getId().equals(GridnodeOptions.getGridnodeKey())) {
					final InetSocketAddress address = ctx.channel().parent()
						.attr(SOCKET_ADDRESS_KEY).get();

					if (address != null) {
						log.atDebug().log(address.toString());
						log.atTrace().log("Seding with port {} from source {}",
							address.getPort(), address.getAddress());
						gridnode.setHostName(address.getAddress().getHostAddress()
							+ ":" + address.getPort());
					}
				}

				for (Gridnode g : gridnodes) {
					if (g.getId().equals(gridnode.getId())) {
						isEmpty = false;
					}
				}

				if (isEmpty) {
					topology.addGridnode(Gridnode.builder().id(gridnode.getId())
						.status(gridnode.getStatus())
						.hostName(gridnode.getHostName()).build());
				} else {
					topology.modifyGridnode(gridnode, g -> {
						log.atDebug().log("Modifying a gridnode");
						g.setStatus(gridnode.getStatus());
						g.setHostName(gridnode.getHostName());
					});
				}
			}
		});
	}

}
