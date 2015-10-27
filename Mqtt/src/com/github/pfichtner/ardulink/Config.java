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
package com.github.pfichtner.ardulink;

import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public abstract class Config {

	private static class ConfigDelegate extends Config {

		private final Config delegate;

		public ConfigDelegate(Config delegate) {
			this.delegate = delegate;
		}

		@Override
		protected String getTopic() {
			return delegate.getTopic();
		}

		public Pattern getTopicPatternDigitalWrite() {
			return delegate.getTopicPatternDigitalWrite();
		}

		public Pattern getTopicPatternAnalogWrite() {
			return delegate.getTopicPatternAnalogWrite();
		}

		public String getTopicPatternDigitalRead() {
			return delegate.getTopicPatternDigitalRead();
		}

		public String getTopicPatternAnalogRead() {
			return delegate.getTopicPatternAnalogRead();
		}

		@Override
		public Pattern getTopicPatternDigitalControl() {
			return delegate.getTopicPatternDigitalControl();
		}

		@Override
		public Pattern getTopicPatternAnalogControl() {
			return delegate.getTopicPatternAnalogControl();
		}

	}

	public static final String DEFAULT_TOPIC = "home/devices/ardulink/";

	public static final Config DEFAULT = withTopic(DEFAULT_TOPIC);

	public static Config withTopic(final String topic) {
		return new Config() {

			@Override
			protected String getTopic() {
				return topic;
			}

			@Override
			public Pattern getTopicPatternDigitalWrite() {
				return compile(write(topic, "D"));
			}

			@Override
			public String getTopicPatternDigitalRead() {
				return read(topic, "D");
			}

			@Override
			public Pattern getTopicPatternAnalogWrite() {
				return compile(write(topic, "A"));
			}

			@Override
			public String getTopicPatternAnalogRead() {
				return read(topic, "A");
			}

			@Override
			public Pattern getTopicPatternDigitalControl() {
				return null;
			}

			@Override
			public Pattern getTopicPatternAnalogControl() {
				return null;
			}

		};
	}

	public Config withTopicPatternAnalogWrite(
			final Pattern withtopicPatternAnalogWrite) {
		return new ConfigDelegate(this) {
			@Override
			public Pattern getTopicPatternAnalogWrite() {
				return withtopicPatternAnalogWrite;
			}
		};
	}

	public Config withTopicPatternAnalogRead(
			final String withtopicPatternAnalogRead) {
		return new ConfigDelegate(this) {
			@Override
			public String getTopicPatternAnalogRead() {
				return withtopicPatternAnalogRead;
			}
		};
	}

	public Config withTopicPatternDigitalWrite(
			final Pattern withtopicPatternDigitalWrite) {
		return new ConfigDelegate(this) {
			@Override
			public Pattern getTopicPatternDigitalWrite() {
				return withtopicPatternDigitalWrite;
			}
		};
	}

	public Config withTopicPatternDigitalRead(
			final String withtopicPatternDigitalRead) {
		return new ConfigDelegate(this) {
			@Override
			public String getTopicPatternDigitalRead() {
				return withtopicPatternDigitalRead;
			}
		};
	}

	public Config withControlChannelEnabled() {
		return new ConfigDelegate(this) {

			private final Pattern topicPatternDigitalControl = compile(write(
					getTopic() + "system/listening/", "D"));

			private final Pattern topicPatternAnalogControl = compile(write(
					getTopic() + "system/listening/", "A"));

			@Override
			public Pattern getTopicPatternDigitalControl() {
				return topicPatternDigitalControl;
			}

			@Override
			public Pattern getTopicPatternAnalogControl() {
				return topicPatternAnalogControl;
			}

		};
	}

	private static String read(String brokerTopic, String prefix) {
		return format(brokerTopic, prefix, "%s", "/get");
	}

	private static String write(String brokerTopic, String prefix) {
		return format(brokerTopic, prefix, "(\\w+)", "/set");
	}

	private static String format(String brokerTopic, String prefix,
			String numerated, String appendix) {
		return brokerTopic + prefix + String.format("%s/value", numerated)
				+ appendix;
	}

	protected abstract String getTopic();

	public abstract Pattern getTopicPatternDigitalWrite();

	public abstract Pattern getTopicPatternAnalogWrite();

	public abstract String getTopicPatternDigitalRead();

	public abstract String getTopicPatternAnalogRead();

	public abstract Pattern getTopicPatternDigitalControl();

	public abstract Pattern getTopicPatternAnalogControl();

	public Config compact() {
		final Config toCompact = this;
		return new Config() {

			private final String topic = toCompact.getTopic();
			private final Pattern topicPatternDigitalWrite = toCompact
					.getTopicPatternDigitalWrite();
			private final String topicPatternDigitalRead = toCompact
					.getTopicPatternDigitalRead();
			private final Pattern topicPatternAnalogWrite = toCompact
					.getTopicPatternAnalogWrite();
			private final String topicPatternAnalogRead = toCompact
					.getTopicPatternAnalogRead();
			private final Pattern topicPatternDigitalControl = toCompact
					.getTopicPatternDigitalControl();
			private final Pattern topicPatternAnalogControl = toCompact
					.getTopicPatternAnalogControl();

			public String getTopic() {
				return topic;
			}

			public Pattern getTopicPatternDigitalWrite() {
				return topicPatternDigitalWrite;
			}

			public String getTopicPatternDigitalRead() {
				return topicPatternDigitalRead;
			}

			public Pattern getTopicPatternAnalogWrite() {
				return topicPatternAnalogWrite;
			}

			public String getTopicPatternAnalogRead() {
				return topicPatternAnalogRead;
			}

			@Override
			public Pattern getTopicPatternDigitalControl() {
				return topicPatternDigitalControl;
			}

			@Override
			public Pattern getTopicPatternAnalogControl() {
				return topicPatternAnalogControl;
			}

		};
	}

}
