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

package org.unigrid.hedgehog.jqwik;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import net.jqwik.api.lifecycle.Lifespan;
import org.apache.commons.io.FileUtils;
import net.jqwik.api.lifecycle.Store;
import org.unigrid.hedgehog.model.Json;

public class TestFileOutput {
	private static final String BUILD_DIRECTORY = System.getProperty("testoutput.target");
	private static final String REPORT_DIRECTORY = "surefire-output";

	@SneakyThrows
	public static void output(String data) {
		final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

		final Optional<StackTraceElement> element = walker.walk(frames ->
			frames.map(StackWalker.StackFrame::toStackTraceElement).filter(e -> {
				return !e.getClassName().endsWith(TestFileOutput.class.getSimpleName());
			}).findFirst()
		);

		if (Objects.nonNull(BUILD_DIRECTORY) && element.isPresent()) {
			final Path path = Path.of(BUILD_DIRECTORY, REPORT_DIRECTORY,
				String.format("%s.%s.txt", element.get().getClassName(),
				element.get().getMethodName())
			);

			/* Uses thje jqwik lifecycle system to not append to file on first run */

			final Store<AtomicInteger> invocations = Store.getOrCreate(path, Lifespan.RUN, () -> {
				return new AtomicInteger(0);
			});

			FileUtils.writeStringToFile(path.toFile(), data.concat("\n"), StandardCharsets.UTF_8,
				invocations.get().intValue() > 0
			);

			invocations.get().incrementAndGet();
		}
	}

	public static <T> void outputJson(T object) throws JsonProcessingException {
		TestFileOutput.output(Json.parse(object));
	}
}
