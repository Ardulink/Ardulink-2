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

package org.ardulink.gui;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.linkmanager.LinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyLinkConfig implements LinkConfig {

	@Named("1_aIntValue")
	private int intValue = 42;

	@Named("2_aBooleanValue")
	private Boolean booleanValue = Boolean.TRUE;

	@Named("3_aStringValue")
	private String stringValue;

	@Named("4_aStringValueWithChoices")
	private String stringValueWithChoices;

	@Named("5_aStringValueWithChoicesIncludingNull")
	private String stringValueWithChoicesIncludingNull;

	@Named("6_aEnumValue")
	private TimeUnit enumValue = TimeUnit.DAYS;

	@ChoiceFor("4_aStringValueWithChoices")
	public List<String> someValuesForChoiceWithoutNull() {
		return Arrays.asList("foo", "bar");
	}

	@ChoiceFor("5_aStringValueWithChoicesIncludingNull")
	public List<String> someValuesForChoiceWithNull() {
		return Arrays.asList("foo", null, "bar");
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public Boolean getBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(Boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public String getStringValueWithChoices() {
		return stringValueWithChoices;
	}

	public void setStringValueWithChoices(String stringValueWithChoices) {
		this.stringValueWithChoices = stringValueWithChoices;
	}

	public String getStringValueWithChoicesIncludingNull() {
		return stringValueWithChoicesIncludingNull;
	}

	public void setStringValueWithChoicesIncludingNull(
			String stringValueWithChoicesIncludingNull) {
		this.stringValueWithChoicesIncludingNull = stringValueWithChoicesIncludingNull;
	}

	public TimeUnit getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(TimeUnit enumValue) {
		this.enumValue = enumValue;
	}

}
