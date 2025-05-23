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

package org.unigrid.hedgehog.model.spork;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.unigrid.hedgehog.model.network.chunk.ChunkData;

@Data @ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class StatisticsPubKey extends GridSpork implements Serializable {
	public StatisticsPubKey() {
		setType(Type.STATISTICS_PUBKEY);

		final StatisticsPubKey.SporkData data = new StatisticsPubKey.SporkData();
		data.setPublicKey(StringUtils.EMPTY);
		setData(data);
	}

	@Data
	public static class SporkData implements ChunkData {
		private String publicKey;

		public SporkData empty() {
			final SporkData data = new SporkData();

			data.setPublicKey(StringUtils.EMPTY);
			return data;
		}
	}
}
