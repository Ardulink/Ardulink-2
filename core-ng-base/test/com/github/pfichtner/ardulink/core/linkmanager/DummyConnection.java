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

package com.github.pfichtner.ardulink.core.linkmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.pfichtner.ardulink.core.Connection;

public class DummyConnection implements Connection {

	private final DummyLinkConfig config;
	private final List<Listener> listeners = new ArrayList<Listener>();
	private AtomicInteger closeCalls = new AtomicInteger();

	public DummyConnection(DummyLinkConfig config) {
		this.config = config;
	}

	@Override
	public void close() throws IOException {
		closeCalls.incrementAndGet();
	}

	public int getCloseCalls() {
		return closeCalls.get();
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	public DummyLinkConfig getConfig() {
		return config;
	}

}