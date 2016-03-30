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

package com.liferay.mobile.sdk.util;

import com.liferay.mobile.sdk.Call;
import com.liferay.mobile.sdk.Config;
import com.liferay.mobile.sdk.ServiceBuilder;
import com.liferay.mobile.sdk.http.Headers;
import com.liferay.mobile.sdk.http.Method;
import com.liferay.mobile.sdk.http.Request;
import com.liferay.mobile.sdk.http.Response;
import com.liferay.mobile.sdk.v2.HttpClient;
import com.liferay.mobile.sdk.v62.portal.PortalService;

/**
 * @author Bruno Farache
 */
public class PortalVersionUtil {

	public static int getPortalVersion(Config config) throws Exception {
		int version = getBuilderNumberHeader(config);

		if (version == PortalVersion.UNKNOWN) {
			try {
				version = getBuilderNumber(config, Config.PATH_62);
			}
			catch (Exception e) {
				version = getBuilderNumber(config, Config.PATH_61);
			}
		}

		return version;
	}

	protected static int getBuilderNumber(Config config, String path)
		throws Exception {

		config.path(path);

		PortalService service = ServiceBuilder.build(PortalService.class);

		int version = PortalVersion.UNKNOWN;

		try {
			version = service.getBuildNumber().execute(config);
		}
		finally {
			config.path(Config.PATH_62);
		}

		return version;
	}

	protected static int getBuilderNumberHeader(Config config)
		throws Exception {

		Request request = Request.url(config.server())
			.method(Method.HEAD)
			.config(config);

		HttpClient client = Call.client();
		Response response = client.sync(request);
		String header = response.getHeaders().get(Headers.LIFERAY_PORTAL);

		if (header == null) {
			return PortalVersion.UNKNOWN;
		}

		int indexOfBuild = header.indexOf("Build");

		if (indexOfBuild == -1) {
			return PortalVersion.UNKNOWN;
		}
		else {
			String buildNumber = header.substring(
				indexOfBuild + 6, indexOfBuild + 10);

			return Integer.valueOf(buildNumber);
		}
	}

}