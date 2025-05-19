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

package org.unigrid.hedgehog.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import me.alexpanov.net.FreePortFinder;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractServer {
	protected URL allocate(String propertyUrl) throws MalformedURLException, UnknownHostException {
		final URL url = new URL(StringUtils.appendIfMissing(propertyUrl, "/"));
		final InetAddress address = InetAddress.getByName(url.getHost());
		final int freePort = FreePortFinder.findFreeLocalPort(url.getPort(), address);

		return new URL(url.getProtocol(), url.getHost(), freePort, url.getFile());
	}

	public abstract Channel getChannel();

	public String getHostName() {
		return ((InetSocketAddress) getChannel().localAddress()).getHostName();
	}

	public int getPort() {
		return ((InetSocketAddress) getChannel().localAddress()).getPort();
	}

	public ChannelId getChannelId() {
		return getChannel().id();
	}
}
