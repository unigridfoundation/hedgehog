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

package org.unigrid.hedgehog.model.network.schedule;

import io.netty.channel.Channel;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.unigrid.hedgehog.model.cdi.CDIUtil;
import org.unigrid.hedgehog.model.network.Node;
import org.unigrid.hedgehog.model.network.Topology;
import org.unigrid.hedgehog.model.network.packet.PublishPeers;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class PublishPeersSchedule extends AbstractSchedule implements Schedulable {
	public PublishPeersSchedule() {
		super(PublishPeers.DISTRIBUTION_FREQUENCY_MINUTES, TimeUnit.MINUTES, false);
	}

	@Override
	public Consumer<Channel> getConsumer() {
		return channel -> {
			CDIUtil.resolveAndRun(Topology.class, topology -> {
				final Set<Node> nodesToSend = topology.cloneNodes();

				log.atTrace().log("Publishing {} peers to {}", nodesToSend.size(), channel.remoteAddress());
				channel.writeAndFlush(PublishPeers.builder().nodes(nodesToSend).build());
			});
		};
	}
}
