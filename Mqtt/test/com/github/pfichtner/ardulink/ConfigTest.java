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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void canCompact() {
		Config orig = Config.withTopic("a").withTopicPatternAnalogRead("b")
				.withTopicPatternAnalogWrite(Pattern.compile("c"))
				.withTopicPatternDigitalRead("d")
				.withTopicPatternDigitalWrite(Pattern.compile("e"));
		Config compacted = orig.compact();
		assertThat(compacted.getTopicPatternAnalogControl(),
				is(orig.getTopicPatternAnalogControl()));
		assertThat(compacted.getTopicPatternAnalogRead(),
				is(orig.getTopicPatternAnalogRead()));
		assertThat(compacted.getTopicPatternAnalogWrite(),
				is(orig.getTopicPatternAnalogWrite()));
		assertThat(compacted.getTopicPatternDigitalControl(),
				is(orig.getTopicPatternDigitalControl()));
		assertThat(compacted.getTopicPatternDigitalRead(),
				is(orig.getTopicPatternDigitalRead()));
		assertThat(compacted.getTopicPatternDigitalWrite(),
				is(orig.getTopicPatternDigitalWrite()));
	}

}
