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

package org.unigrid.hedgehog.server.rest;

import io.findify.s3mock.S3Mock;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.SneakyThrows;
import net.jqwik.api.Disabled;
import net.jqwik.api.Example;
import net.jqwik.api.lifecycle.AfterTry;
import net.jqwik.api.lifecycle.BeforeTry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.unigrid.hedgehog.client.ResponseOddityException;
import org.unigrid.hedgehog.client.RestClient;
import org.unigrid.hedgehog.model.s3.entity.CopyObjectResult;
import org.unigrid.hedgehog.model.s3.entity.CreateBucketConfiguration;
import org.unigrid.hedgehog.model.s3.entity.ListBucketResult;

public class StorageObjectTest extends BaseRestClientTest {
	S3Mock api;

	String bucket = "testBucket";
	String key = "testObject";
	String copy = "copied";
	final int MAX_KEYS = 10000;

	@BeforeTry
	public void beforeApiTry() {
		api = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
		api.start();
	}

	@AfterTry
	public void afterApiTry() {
		api.shutdown();
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldCreateObject() {
		final RestClient clientMock = new RestClient(server.getRest().getHostName(), 8001, false);
		final CreateBucketConfiguration config = new CreateBucketConfiguration("TestConfig");

		clientMock.put("/" + bucket, Entity.xml(config));
		client.put("/bucket/" + bucket, Entity.xml(config));

		final InputStream inputStream = new ByteArrayInputStream("Hello, World!".getBytes());
		final Entity entity = Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM);
		final InputStream inputStream2 = new ByteArrayInputStream("Hello, World!".getBytes());
		final Entity entity2 = Entity.entity(inputStream, MediaType.APPLICATION_OCTET_STREAM);

		Response mockResponse = clientMock.post("/" + bucket + "/" + key, entity);
		Response response = client.post("/storage-object/" + bucket + "/" + key, entity2);

		assertThat(response.getStatus(), equalTo(mockResponse.getStatus()));
		clientMock.close();
	}

	@Example
	@SneakyThrows
	public void shouldHaveInputStream() {
		try {
			client.post("/storage-object/" + bucket + "/" + key, Entity.text(""));
		} catch (Exception e) {
			assertThat(e, isA(ResponseOddityException.class));
		}
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldListObjects() {
		ListBucketResult response = client.getEntity("/storage-object/list/" + bucket, ListBucketResult.class);

		assertThat(response.getName(), equalTo(bucket));
		assertThat(response.getDelimiter(), equalTo(""));
		assertThat(response.getPrefix(), equalTo(""));
		assertThat(response.getMaxKeys(), equalTo(MAX_KEYS));
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldCopyAndReturnXML() {
		final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.add("x-amz-copy-source", "/" + bucket + "/" + key);

		final Response response = client.putWithHeaders("/storage-object/" + bucket + "/" + copy,
			Entity.text(""), headers
		);

		CopyObjectResult result = response.readEntity(CopyObjectResult.class);

		assertThat(response.getStatus(), equalTo(200));
		assertThat(result.getETag(), is(notNullValue()));
		assertThat(result.getLastModified().toString(), is(notNullValue()));
	}

	@Example
	@SneakyThrows
	public void shouldContainHeader() {
		final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		try {
			client.putWithHeaders("/storage-object/testBukcet/testObjecyt", Entity.text(""), headers);
		} catch (Exception e) {
			assertThat(e.getMessage(), containsString("400 Bad Request"));
		}
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldReturnData() {
		final Response response = client.get("/storage-object/" + bucket + "/" + key);

		assertThat(response.getStatus(), equalTo(200));
		// file should not be empty
		assertThat(response.getLength(), greaterThan(0));
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldExist() {
		try {
			client.get("/storage-object/" + bucket + "/testtest");
		} catch (Exception e) {
			assertThat(e, isA(ResponseOddityException.class));
			assertThat(e.getMessage(), containsString("404 Not Found"));
		}
	}

	@Example
	@Disabled
	@SneakyThrows
	public void shouldReturnNoContent() {
		Response response = client.delete("/storage-object/" + bucket + "/" + copy);

		assertThat(response.getLength(), equalTo(-1));
		assertThat(response.getStatus(), equalTo(204));
	}
}
