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
 * @author Peter Fichtner
 * 
 * [adsense]
 */
public abstract class Config {

	public static final String DEFAULT_TOPIC = "home/devices/ardulink/";

	public static final Config DEFAULT = withTopic(DEFAULT_TOPIC);

	public static Config withTopic(final String topic) {
		return new Config() {

			private final Pattern topicPatternDigitalWrite;
			private final String topicPatternDigitalRead;
			private final Pattern topicPatternAnalogWrite;
			private final String topicPatternAnalogRead;

			{
				this.topicPatternDigitalWrite = compile(write(topic, "D"));
				this.topicPatternDigitalRead = read(topic, "D");
				this.topicPatternAnalogWrite = compile(write(topic, "A"));
				this.topicPatternAnalogRead = read(topic, "A");
			}

			private String read(String brokerTopic, String prefix) {
				return format(brokerTopic, prefix, "%s", "/get");
			}

			private String write(String brokerTopic, String prefix) {
				return format(brokerTopic, prefix, "(\\w+)", "/set");
			}

			private String format(String brokerTopic, String prefix,
					String numerated, String appendix) {
				return brokerTopic + prefix
						+ String.format("%s/value", numerated) + appendix;
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

		};
	}

	public abstract Pattern getTopicPatternDigitalWrite();

	public abstract Pattern getTopicPatternAnalogWrite();

	public abstract String getTopicPatternDigitalRead();

	public abstract String getTopicPatternAnalogRead();

}
