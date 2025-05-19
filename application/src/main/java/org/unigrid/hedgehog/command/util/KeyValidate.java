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

package org.unigrid.hedgehog.command.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.unigrid.hedgehog.model.crypto.Signature;
import org.unigrid.hedgehog.model.crypto.VerifySignatureException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "key-validate")
public class KeyValidate implements Runnable {
	@Getter @Option(names = { "-D", "--data" }, description = "Hex representation of data to verify.", required = true)
	private String data;

	@Getter @Option(names = { "-k", "--key" }, required = true,
		description = "Hex representation of public key to use."
	)
	private String key;

	@Getter @Option(names = { "-s", "--signature" }, required = true,
		description = "Hex representation of signature to verify."
	)
	private String signatureHex;

	@Override
	public void run() {
		try {
			final Signature signature = new Signature(Optional.empty(), Optional.of(key));
			System.out.println(signature.verify(Hex.decodeHex(data), Hex.decodeHex(signatureHex)));

		} catch (DecoderException | InvalidAlgorithmParameterException | InvalidKeySpecException
			| NoSuchAlgorithmException | VerifySignatureException ex) {

			System.err.println(String.format("Failed to verify signature: %s", ex));
		}
	}
}
