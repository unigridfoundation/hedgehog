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

package org.unigrid.hedgehog.command.cli;

import org.unigrid.hedgehog.command.util.RestClientCommand;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.unigrid.hedgehog.model.Json;
import org.unigrid.hedgehog.model.network.Node;
import picocli.CommandLine.Command;

@Command(name = "node-list")
public class NodeList extends RestClientCommand {
	public NodeList() {
		super(HttpMethod.GET, "/node", Optional.of(() -> new HashSet<Node>()));
	}

	@Override
	protected void execute(Response response) {
		System.out.println(Json.parse(response.readEntity(new GenericType<Set<Node>>() { })));
	}
}
