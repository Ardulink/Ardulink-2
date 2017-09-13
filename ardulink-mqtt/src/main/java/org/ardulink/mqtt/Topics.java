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
public abstract class Topics {

	public static class DefaultTopics extends Topics {

		private final String topic;
		private Pattern topicPatternDigitalWrite;
		private String topicPatternDigitalRead;
		private Pattern topicPatternAnalogWrite;
		private String topicPatternAnalogRead;
		private Pattern topicPatternDigitalControl;
		private Pattern topicPatternAnalogControl;

		private DefaultTopics(String topic) {
			this.topic = topic.endsWith("/") ? topic : topic + "/";
			this.topicPatternDigitalWrite = write(getTopic() + "D%s");
			this.topicPatternDigitalRead = read(getTopic() + "D%s");
			this.topicPatternAnalogWrite = write(getTopic() + "A%s");
			this.topicPatternAnalogRead = read(getTopic() + "A%s");
		}

		private DefaultTopics(Topics t) {
			this.topic = t.getTopic();
			this.topicPatternDigitalWrite = t.getTopicPatternDigitalWrite();
			this.topicPatternDigitalRead = t.getTopicPatternDigitalRead();
			this.topicPatternAnalogWrite = t.getTopicPatternAnalogWrite();
			this.topicPatternAnalogRead = t.getTopicPatternAnalogRead();
			this.topicPatternDigitalControl = t.getTopicPatternDigitalControl();
			this.topicPatternAnalogControl = t.getTopicPatternAnalogControl();
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

		public static Topics copyOf(Topics topics) {
			return typedCopy(topics);
		}

		private static DefaultTopics typedCopy(Topics topics) {
			return new DefaultTopics(topics);
		}

		public static Topics withTopic(String topic) {
			return new DefaultTopics(topic);
		}

	}

	public static final String DEFAULT_BASE_TOPIC = "home/devices/ardulink/";

	public static Topics basedOn(String topic) {
		return DefaultTopics.withTopic(topic);
	}

	public static Topics withSeparateReadWriteTopics(String topic) {
		Topics topics = basedOn(topic);
		String aw = "/value/set";
		String ar = "/value/get";
		String norm = topics.getTopic();
		return topics.withTopicPatternAnalogWrite(write(norm + "A%s" + aw))
				.withTopicPatternDigitalWrite(write(norm + "D%s" + aw))
				.withTopicPatternAnalogRead(read(norm + "A%s" + ar))
				.withTopicPatternDigitalRead(read(norm + "D%s" + ar));
	}

	public Topics withTopicPatternAnalogWrite(Pattern topicPatternAnalogWrite) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
		copy.topicPatternAnalogWrite = topicPatternAnalogWrite;
		return copy;
	}

	public Topics withTopicPatternAnalogRead(String topicPatternAnalogRead) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
		copy.topicPatternAnalogRead = topicPatternAnalogRead;
		return copy;
	}

	public Topics withTopicPatternDigitalWrite(Pattern topicPatternDigitalWrite) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
		copy.topicPatternDigitalWrite = topicPatternDigitalWrite;
		return copy;
	}

	public Topics withTopicPatternDigitalRead(String topicPatternDigitalRead) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
		copy.topicPatternDigitalRead = topicPatternDigitalRead;
		return copy;
	}

	public Topics withControlChannelEnabled() {
		String prefix = "system/listening/";
		return DefaultTopics
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

	public Topics withTopicPatternDigitalControl(String write) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
		copy.topicPatternDigitalControl = compile(write);
		return copy;
	}

	public Topics withTopicPatternAnalogControl(String write) {
		DefaultTopics copy = DefaultTopics.typedCopy(this);
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
