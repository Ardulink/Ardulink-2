package com.github.pfichtner.ardulink;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.regex.Pattern;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void canCompact() {
		Config c1 = Config.withTopic("a").withTopicPatternAnalogRead("b")
				.withTopicPatternAnalogWrite(Pattern.compile("c"))
				.withTopicPatternDigitalRead("d")
				.withTopicPatternDigitalWrite(Pattern.compile("e"));
		Config c2 = c1.compact();
		assertThat(c2.getTopicPatternAnalogControl(),
				is(c1.getTopicPatternAnalogControl()));
		assertThat(c2.getTopicPatternAnalogRead(),
				is(c1.getTopicPatternAnalogRead()));
		assertThat(c2.getTopicPatternAnalogWrite(),
				is(c1.getTopicPatternAnalogWrite()));
		assertThat(c2.getTopicPatternDigitalControl(),
				is(c1.getTopicPatternDigitalControl()));
		assertThat(c2.getTopicPatternDigitalRead(),
				is(c1.getTopicPatternDigitalRead()));
		assertThat(c2.getTopicPatternDigitalWrite(),
				is(c1.getTopicPatternDigitalWrite()));
	}

}
