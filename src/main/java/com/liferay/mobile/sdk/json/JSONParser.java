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

package com.liferay.mobile.sdk.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.lang.reflect.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Bruno Farache
 */
public class JSONParser {

	public static <T> T fromJSON(JsonElement json, Type type) throws Exception {
		return gson().fromJson(json, type);
	}

	public static <T> T fromJSON(String json, Type type) throws Exception {
		return gson().fromJson(json, type);
	}

	public static JsonElement parse(String json) {
		return parser.parse(json);
	}

	public synchronized static void registerTypeAdapter(
		Type type, Object adapter) {

		adapters.put(type, adapter);
		gson = null;
	}

	protected synchronized static Gson gson() {
		if (gson == null) {
			GsonBuilder builder = new GsonBuilder();

			for (Entry<Type, Object> entry : adapters.entrySet()) {
				builder.registerTypeAdapter(entry.getKey(), entry.getValue());
			}

			gson = builder.create();
		}

		return gson;
	}

	protected static Map<Type, Object> adapters = new HashMap<>();
	protected static Gson gson;
	protected static JsonParser parser = new JsonParser();

	static {
		registerTypeAdapter(JSONArray.class, new JSONArrayDeserializer());
		registerTypeAdapter(JSONObject.class, new JSONObjectDeserializer());
	}

}