/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.util;

import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.stream.Collectors.joining;
import static org.ardulink.util.URIs.encode;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class URIBuilder {

	private final String base;
	private final Map<String, Object> params = new LinkedHashMap<>();

	public static URIBuilder uriBuilder(String protocol, String host) {
		return new URIBuilder(protocol, host);
	}

	public static URIBuilder uriBuilder(String base) {
		return new URIBuilder(base);
	}

	private URIBuilder(String base) {
		this.base = base;
	}

	private URIBuilder(String protocol, String host) {
		this(protocol + "://" + host);
	}

	public URIBuilder param(String name, Object value) {
		this.params.put(name, value);
		return this;
	}

	public URIBuilder params(Map<String, ?> params) {
		this.params.putAll(params);
		return this;
	}

	public URI build() {
		return create(params.isEmpty() ? base : base + "?" + params());
	}

	private String params() {
		return params.entrySet().stream().map(URIBuilder::entryToString).collect(joining("&"));
	}

	private static String entryToString(Entry<String, Object> entry) {
		return format("%s=%s", encode(entry.getKey()), encode(String.valueOf(entry.getValue())));
	}

}