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
package org.ardulink.extendedfeatures.deviceid;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.util.Preconditions.checkState;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.events.DefaultRplyEvent;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.qos.ResponseAwaiter;
import org.ardulink.core.virtual.VirtualLink;
import org.ardulink.util.MapBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/ This XFeature is able to retrieve
 * the Unique ID from an Arduino based board. If arduino doesn't have an unique
 * id this class suggests an id. Then in the reply message between parameters
 * DeviceID searches for a UniqueID parameter.
 * 
 * [adsense]
 *
 */
public class DeviceIDTest {

	private class VirtualLinkWithUniqueIdSupport extends VirtualLink {

		private int messageId = 42;

		protected final Queue<DefaultRplyEvent> queue = new LinkedList<DefaultRplyEvent>();

		public VirtualLinkWithUniqueIdSupport() {
			super(NO_ATTRIBUTES);
		}

		@Override
		public long sendCustomMessage(String... messages) throws IOException {
			checkState(GET_UNIQUE_ID_CUSTOM_MESSAGE.equals(messages[0]),
					"Only getUniqueID supported, got %s", messages[0]);
			checkState(messages.length == 2,
					"Expected 2 parameters, got %s (%s)", messages.length,
					Arrays.asList(messages));
			queueUniquedIdRply(messageId);
			return messageId;
		}

		protected void queueUniquedIdRply(int messageId) {
			queueRply(createUniquedIdRply(messageId));
		}

		protected void queueRply(DefaultRplyEvent event) {
			synchronized (queue) {
				queue.add(event);
			}
		}

		@Override
		protected void sendRandomMessagesAndSleep() {
			if (queue != null) {
				synchronized (queue) {
					for (DefaultRplyEvent event : queue) {
						fireReplyReceived(event);
					}
				}
			}
		}
	}

	private static final String SOME_UNIQUE_ID = "an-uuid-created-by-arduino";

	private static final String GET_UNIQUE_ID_CUSTOM_MESSAGE = "getUniqueID";

	private static final String KEY_UNIQUE_ID = "UniqueID";

	@Rule
	public Timeout timeout = new Timeout(15, SECONDS);

	private final VirtualLinkWithUniqueIdSupport link = new VirtualLinkWithUniqueIdSupport();

	private DefaultRplyEvent createUniquedIdRply(int messageId) {
		return new DefaultRplyEvent(true, messageId, MapBuilder
				.<String, Object> newMapBuilder()
				.put(KEY_UNIQUE_ID, SOME_UNIQUE_ID).build());
	}

	@Test
	public void canSendAndReceiveUniqueId() throws IOException {
		RplyEvent rplyEvent = ResponseAwaiter.onLink(link).waitForResponse(
				sendAgetUniqueIdCustomMsg(link));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	@Test
	public void canSendAndReceiveUniqueIdWithShortTimeout() throws IOException {
		RplyEvent rplyEvent = ResponseAwaiter.onLink(link)
				.withTimeout(500, MILLISECONDS)
				.waitForResponse(sendAgetUniqueIdCustomMsg(link));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	@Test(expected = IllegalStateException.class)
	public void whenReplyTooksTooLongAtimeoutOccurs() throws IOException {
		VirtualLinkWithUniqueIdSupport link = linkThatDelaysResponse(3, SECONDS);
		RplyEvent rplyEvent = ResponseAwaiter.onLink(link)
				.withTimeout(500, MILLISECONDS)
				.waitForResponse(sendAgetUniqueIdCustomMsg(link));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	@Test
	public void whenReplyTooksLongerNoTimeout() throws IOException {
		VirtualLinkWithUniqueIdSupport link = linkThatDelaysResponse(3, SECONDS);
		RplyEvent rplyEvent = ResponseAwaiter.onLink(link)
				.withTimeout(5, SECONDS)
				.waitForResponse(sendAgetUniqueIdCustomMsg(link));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	@Test
	public void aMessageInsertedBeforeDoesNotInference() throws IOException {
		VirtualLinkWithUniqueIdSupport link = new VirtualLinkWithUniqueIdSupport() {
			@Override
			protected void queueUniquedIdRply(final int messageId) {
				queueRply(createUniquedIdRply(messageId - 1));
				super.queueUniquedIdRply(messageId);
			}
		};
		RplyEvent rplyEvent = ResponseAwaiter.onLink(link).waitForResponse(
				sendAgetUniqueIdCustomMsg(link));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	@Test
	public void aMessageInsertedAfterDoesNotInference() throws IOException {
		VirtualLinkWithUniqueIdSupport myLink = new VirtualLinkWithUniqueIdSupport() {
			@Override
			protected void queueUniquedIdRply(final int messageId) {
				super.queueUniquedIdRply(messageId);
				queueRply(createUniquedIdRply(messageId + 1));
			}
		};
		RplyEvent rplyEvent = ResponseAwaiter.onLink(myLink).waitForResponse(
				sendAgetUniqueIdCustomMsg(myLink));
		assertThat(uniqueIdResponse(rplyEvent), is(SOME_UNIQUE_ID));

	}

	private long sendAgetUniqueIdCustomMsg(VirtualLinkWithUniqueIdSupport myLink)
			throws IOException {
		return myLink.sendCustomMessage(GET_UNIQUE_ID_CUSTOM_MESSAGE,
				anySuggestedUuid());
	}

	private String anySuggestedUuid() {
		return "an-uuid-suggestion";
	}

	private String uniqueIdResponse(RplyEvent rplyEvent) {
		return (String) rplyEvent.getParameterValue(KEY_UNIQUE_ID);
	}

	private VirtualLinkWithUniqueIdSupport linkThatDelaysResponse(
			final int delay, final TimeUnit timeUnit) {
		return new VirtualLinkWithUniqueIdSupport() {
			@Override
			protected void queueUniquedIdRply(final int messageId) {
				Executors.newFixedThreadPool(1).submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						timeUnit.sleep(delay);
						synchronized (queue) {
							queue.add(new DefaultRplyEvent(true, messageId,
									MapBuilder.<String, Object> newMapBuilder()
											.put(KEY_UNIQUE_ID, SOME_UNIQUE_ID)
											.build()));
						}
						return null;
					}
				});
			}
		};
	}

}
