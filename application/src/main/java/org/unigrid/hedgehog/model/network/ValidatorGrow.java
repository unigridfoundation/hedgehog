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

package org.unigrid.hedgehog.model.network;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ValidatorGrow {
	private String pubKey;
	private String data;
	private List<String> signatures;

	public List<byte[]> getByteSignatures() {
		List<byte[]> signs = new ArrayList<>();
		for (String s : signatures) {
			try {
				signs.add(Hex.decodeHex(s));
			} catch (DecoderException ex) {
				log.atError().log(ex.getMessage());
			}
		}
		return signs;
	}

	public byte[] getByteData() {
		try {
			return Hex.decodeHex(data);
		} catch (DecoderException ex) {
			log.atError().log(ex.getMessage());
			String s = "";
			return s.getBytes();
		}
	}
}
