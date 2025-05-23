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

import io.netty.channel.ChannelHandlerContext;
import java.net.UnknownHostException;
import lombok.SneakyThrows;
import mockit.Mocked;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import static com.shazam.shazamcrest.matcher.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import java.net.InetSocketAddress;
import java.util.Optional;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.Pair;
import org.unigrid.hedgehog.jqwik.ArbitraryGenerator;
import org.unigrid.hedgehog.model.network.Connection;
import org.unigrid.hedgehog.model.network.Node;
import org.unigrid.hedgehog.model.network.packet.Ping;
import org.unigrid.hedgehog.model.network.packet.PublishPeers;

public class PublishPeersIntegrityTest extends BaseCodecTest<PublishPeers> {
	@Mocked
	private Connection emptyConnection;

	@Provide
	public Arbitrary<PublishPeers> providePublishPeers(@ForAll @Positive byte nodes,
		@ForAll @IntRange(min = 4097, max = 65535) int port) throws UnknownHostException {

		final PublishPeers pp = PublishPeers.builder().build();

		for (int i = 0; i < nodes; i++) {
			final String ip = ArbitraryGenerator.ip4();
			final InetSocketAddress socketAddress = new InetSocketAddress(ip, port);
			final Node node = Node.builder().address(socketAddress).build();
			pp.getNodes().add(node);
		}

		return Arbitraries.of(pp);
	}

	@Provide
	public Arbitrary<PublishPeers> provideWithConnection(@ForAll("providePublishPeers") PublishPeers publishPeers) {
		for (Node n : publishPeers.getNodes()) {
			n.setConnection(Optional.of(emptyConnection));
		}

		return Arbitraries.of(publishPeers);
	}

	@Property
	@SneakyThrows
	public void shouldMatch(@ForAll("providePublishPeers") PublishPeers publishPeers,
		@Mocked ChannelHandlerContext context) {

		final Optional<Pair<MutableInt, MutableInt>> sizes = getSizeHolder();

		final PublishPeers resultingPublishPeers = encodeDecode(publishPeers,
			new PublishPeersEncoder(), new PublishPeersDecoder(), context, sizes
		);

		assertThat(resultingPublishPeers, sameBeanAs(publishPeers));
		assertThat(resultingPublishPeers, equalTo(publishPeers));
		assertThat(sizes.get().getLeft(), equalTo(sizes.get().getRight()));
	}

	@SneakyThrows
	@Property(tries = 50)
	public void shouldNotIncludeConnectionOrPing(@ForAll("providePublishPeers") PublishPeers publishPeers,
		@Mocked ChannelHandlerContext context) {

		for (Node n : publishPeers.getNodes()) {
			n.setConnection(Optional.of(emptyConnection));
		}

		final PublishPeers resultingPublishPeers = encodeDecode(publishPeers,
			new PublishPeersEncoder(), new PublishPeersDecoder(), context
		);

		for (Node n : resultingPublishPeers.getNodes()) {
			assertThat(n.getConnection(), equalTo(Optional.empty()));
		}
	}
}
