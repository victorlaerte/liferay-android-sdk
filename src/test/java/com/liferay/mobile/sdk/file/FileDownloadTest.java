/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.mobile.sdk.file;

import com.liferay.mobile.sdk.BaseTest;
import com.liferay.mobile.sdk.Config;
import com.liferay.mobile.sdk.DLAppServiceTest;
import com.liferay.mobile.sdk.TestCallback;
import com.liferay.mobile.sdk.auth.BasicAuthentication;
import com.liferay.mobile.sdk.auth.DigestAuthentication;
import com.liferay.mobile.sdk.http.Response;
import com.liferay.mobile.sdk.http.Status;
import com.liferay.mobile.sdk.util.PortalVersion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.concurrent.CountDownLatch;

import org.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Bruno Farache
 */
public class FileDownloadTest extends BaseTest {

	public FileDownloadTest() throws IOException {
		super();
	}

	@Test
	public void cancel() throws Exception {
		JSONObject file = FileUploadTest.uploadPhoto(props);

		try {
			Config config = getDigestAuthenticationConfig();

			String url = config.server() +
				"/webdav/guest/document_library/" +
				file.getString(DLAppServiceTest.TITLE);

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			FileProgressCallback progressCallback = new FileProgressCallback() {

				@Override
				public void onBytes(byte[] bytes) {
					try {
						baos.write(bytes);
					}
					catch (IOException ioe) {
						fail(ioe.getMessage());
					}
				}

				@Override
				public void onProgress(int totalBytes) {
					setCancelled(true);
				}

			};

			Response response = DownloadUtil.download(
				config, url, null, progressCallback);

			assertNotNull(response);
			assertEquals(Status.OK, response.statusCode());
			assertTrue(baos.size() < 2048);
		}
		finally {
			if (file != null) {
				new DLAppServiceTest().deleteFileEntry(
					file.getLong(DLAppServiceTest.FILE_ENTRY_ID));
			}
		}
	}

	@Test
	public void downloadAsync() throws Exception {
		Config config = getDigestAuthenticationConfig();

		String url = config.server() + "/webdav/guest/document_library/" +
			file.getString(DLAppServiceTest.TITLE);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final CountDownLatch lock = new CountDownLatch(1);

		FileProgressCallback progressCallback = new FileProgressCallback() {

			@Override
			public void onBytes(byte[] bytes) {
				try {
					baos.write(bytes);
				}
				catch (IOException ioe) {
					fail(ioe.getMessage());
				}
			}

			@Override
			public void onProgress(int totalBytes) {
				if (totalBytes == 5) {
					try {
						baos.flush();
					}
					catch (IOException ioe) {
						fail(ioe.getMessage());
					}
				}
			}

		};

		TestCallback<Response> callback = new TestCallback<>(lock);
		DownloadUtil.download(config, url, callback, progressCallback);

		await(lock);
		assertEquals(Status.OK, callback.result().statusCode());
		assertEquals(5, baos.size());
	}

	@Test
	public void downloadSync() throws Exception {
		Config config = getDigestAuthenticationConfig();

		String url = config.server() + "/webdav/guest/document_library/" +
			file.getString(DLAppServiceTest.TITLE);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		FileProgressCallback progressCallback = new FileProgressCallback() {

			@Override
			public void onBytes(byte[] bytes) {
				try {
					baos.write(bytes);
				}
				catch (IOException ioe) {
					fail(ioe.getMessage());
				}
			}

			@Override
			public void onProgress(int totalBytes) {
				if (totalBytes == 5) {
					try {
						baos.flush();
					}
					catch (IOException ioe) {
						fail(ioe.getMessage());
					}
				}
			}

		};

		Response response = DownloadUtil.download(
			config, url, null, progressCallback);

		assertNotNull(response);
		assertEquals(Status.OK, response.statusCode());
		assertEquals(5, baos.size());
	}

	@Test
	public void getDownloadURL() throws Exception {
		Config config = Config.global();

		String expectedURL = "http://localhost:8080/webdav/guest" +
			"/document_library" +
			"/folder%20with%20spaces" +
			"/file%20%C3%A1%C3%A9%C3%AD%C3%B2%C3%BA%C3%B1.txt";

		String downloadURL = DownloadUtil.getWebDAVFileURL(
			config, PortalVersion.V_6_2, "/guest", "/folder with spaces",
			"file áéíòúñ.txt");

		assertEquals(expectedURL, downloadURL);

		downloadURL = DownloadUtil.getWebDAVFileURL(
			config, PortalVersion.V_6_2, "guest", "folder with spaces",
			"/file áéíòúñ.txt");

		assertEquals(expectedURL, downloadURL);

		expectedURL = "http://localhost:8080/webdav/guest" +
			"/document_library" +
			"/file%20%C3%A1%C3%A9%C3%AD%C3%B2%C3%BA%C3%B1.txt";

		downloadURL = DownloadUtil.getWebDAVFileURL(
			config, PortalVersion.V_6_2, "guest", "", "file áéíòúñ.txt");

		assertEquals(expectedURL, downloadURL);
	}

	@Before
	public void setUp() throws Exception {
		DLAppServiceTest test = new DLAppServiceTest();
		file = test.addFileEntry();
	}

	@After
	public void tearDown() throws Exception {
		if (file != null) {
			DLAppServiceTest test = new DLAppServiceTest();
			test.deleteFileEntry(file.getLong(DLAppServiceTest.FILE_ENTRY_ID));
		}
	}

	protected Config getDigestAuthenticationConfig() {
		Config global = Config.global();
		BasicAuthentication basic = (BasicAuthentication)global.auth();
		DigestAuthentication digest = new DigestAuthentication(
			basic.username(), basic.password());

		return global.newBuilder().auth(digest).build();
	}

	protected JSONObject file;

}