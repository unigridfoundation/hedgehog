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

import java.time.Instant;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.api.Example;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.unigrid.hedgehog.jqwik.BaseMockedWeldTest;

public class GridSporkTest extends BaseMockedWeldTest {
	final GridSpork gridSpork = new GridSpork();

	@BeforeProperty
	public void before() {
		gridSpork.setTimeStamp(Instant.now());
	}

	@Example
	public void shouldBeNewerIfOtherIsNull() {
		assertThat(gridSpork.isNewerThan(null), is(true));
	}

	@Example
	public void shouldBeNewerIfOtherTimeStampIsNull() {
		final GridSpork otherGridSpork = new GridSpork();

		assertThat(gridSpork.isNewerThan(otherGridSpork), is(true));
	}

	@Example
	public void shouldBeNewerIfOtherIsOlder() {
		final GridSpork otherGridSpork = new GridSpork();
		otherGridSpork.setTimeStamp(gridSpork.getTimeStamp().minusSeconds(60));

		assertThat(gridSpork.isNewerThan(otherGridSpork), is(true));
	}

	@Example
	public void shouldNotBeNewerIfTimeStampIsNull() {
		final GridSpork otherGridSpork = new GridSpork();
		otherGridSpork.setTimeStamp(gridSpork.getTimeStamp());
		gridSpork.setTimeStamp(null);

		assertThat(gridSpork.isNewerThan(otherGridSpork), is(false));
	}

	@Example
	public void shouldNotBeNewerIfOtherIsNewer() {
		final GridSpork otherGridSpork = new GridSpork();
		otherGridSpork.setTimeStamp(gridSpork.getTimeStamp().plusSeconds(60));

		assertThat(gridSpork.isNewerThan(otherGridSpork), is(false));
	}

	@Example
	public void shouldNotBeNewerIfAllTimeStampsNull() {
		final GridSpork otherGridSpork = new GridSpork();
		otherGridSpork.setTimeStamp(null);
		gridSpork.setTimeStamp(null);

		assertThat(gridSpork.isNewerThan(otherGridSpork), is(false));
	}
}
