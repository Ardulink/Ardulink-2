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
package org.ardulink.mqtt;

import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class Config {

	public static class DefaultConfig extends Config {

		private final String topic;
		private Pattern topicPatternDigitalWrite;
		private String topicPatternDigitalRead;
		private Pattern topicPatternAnalogWrite;
		private String topicPatternAnalogRead;
		private Pattern topicPatternDigitalControl;
		private Pattern topicPatternAnalogControl;

		private DefaultConfig(String topic) {
			this.topic = topic.endsWith("/") ? topic : topic + "/";
			this.topicPatternDigitalWrite = write(getTopic() + "D%s");
			this.topicPatternDigitalRead = read(getTopic() + "D%s");
			this.topicPatternAnalogWrite = write(getTopic() + "A%s");
			this.topicPatternAnalogRead = read(getTopic() + "A%s");
		}

		private DefaultConfig(Config c) {
			this.topic = c.getTopic();
			this.topicPatternDigitalWrite = c.getTopicPatternDigitalWrite();
			this.topicPatternDigitalRead = c.getTopicPatternDigitalRead();
			this.topicPatternAnalogWrite = c.getTopicPatternAnalogWrite();
			this.topicPatternAnalogRead = c.getTopicPatternAnalogRead();
			this.topicPatternDigitalControl = c.getTopicPatternDigitalControl();
			this.topicPatternAnalogControl = c.getTopicPatternAnalogControl();
		}

		@Override
		public String getTopic() {
			return topic;
		}

		@Override
		public Pattern getTopicPatternDigitalWrite() {
			return topicPatternDigitalWrite;
		}

		@Override
		public String getTopicPatternDigitalRead() {
			return topicPatternDigitalRead;
		}

		@Override
		public Pattern getTopicPatternAnalogWrite() {
			return topicPatternAnalogWrite;
		}

		@Override
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

		public static Config copyOf(Config config) {
			return typedCopy(config);
		}

		private static DefaultConfig typedCopy(Config config) {
			return new DefaultConfig(config);
		}

		public static Config withTopic(String topic) {
			DefaultConfig config = new DefaultConfig(topic);
			String aw = "/value/set";
			String ar = "/value/get";
			String norm = config.getTopic();
			return config.withTopicPatternAnalogWrite(write(norm + "A%s" + aw))
					.withTopicPatternDigitalWrite(write(norm + "D%s" + aw))
					.withTopicPatternAnalogRead(read(norm + "A%s" + ar))
					.withTopicPatternDigitalRead(read(norm + "D%s" + ar));
		}

	}

	public static final String DEFAULT_TOPIC = "home/devices/ardulink/";

	public static final Config DEFAULT = withTopic(DEFAULT_TOPIC);

	public static Config withTopic(String topic) {
		return DefaultConfig.withTopic(topic);

	}

	public Config withTopicPatternAnalogWrite(Pattern topicPatternAnalogWrite) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternAnalogWrite = topicPatternAnalogWrite;
		return copy;
	}

	public Config withTopicPatternAnalogRead(String topicPatternAnalogRead) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternAnalogRead = topicPatternAnalogRead;
		return copy;
	}

	public Config withTopicPatternDigitalWrite(Pattern topicPatternDigitalWrite) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternDigitalWrite = topicPatternDigitalWrite;
		return copy;
	}

	public Config withTopicPatternDigitalRead(String topicPatternDigitalRead) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternDigitalRead = topicPatternDigitalRead;
		return copy;
	}

	public Config withControlChannelEnabled() {
		String prefix = "system/listening/";
		return DefaultConfig
				.typedCopy(this)
				.withTopicPatternDigitalControl(
						prefix(getTopicPatternDigitalWrite(), prefix))
				.withTopicPatternAnalogControl(
						prefix(getTopicPatternAnalogWrite(), prefix));
	}

	private String prefix(Pattern writePattern, String prefix) {
		return new StringBuilder(writePattern.pattern()).insert(
				getTopic().length(), prefix).toString();
	}

	public Config withTopicPatternDigitalControl(String write) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternDigitalControl = compile(write);
		return copy;
	}

	public Config withTopicPatternAnalogControl(String write) {
		DefaultConfig copy = DefaultConfig.typedCopy(this);
		copy.topicPatternAnalogControl = compile(write);
		return copy;
	}

	private static Pattern write(String format) {
		return compile(String.format(format, "(\\w+)"));
	}

	private static String read(String format) {
		return String.format(format, "%s");
	}

	protected abstract String getTopic();

	public abstract Pattern getTopicPatternDigitalWrite();

	public abstract Pattern getTopicPatternAnalogWrite();

	public abstract String getTopicPatternDigitalRead();

	public abstract String getTopicPatternAnalogRead();

	public abstract Pattern getTopicPatternDigitalControl();

	public abstract Pattern getTopicPatternAnalogControl();

}
