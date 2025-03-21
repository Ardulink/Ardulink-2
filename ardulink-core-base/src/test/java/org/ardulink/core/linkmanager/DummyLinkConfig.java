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

package org.ardulink.core.linkmanager;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.core.proto.api.Protocols.protocolNames;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2;

@I18n("message")
public class DummyLinkConfig implements LinkConfig {
	
	public static final String XXX = "xxx";

	public String a;

	public int b = 42;

	@Named("c")
	public String c;

	public Protocol protocol = protoByName(ArdulinkProtocol2.NAME);

	@Named("d")
	public String d;

	@Named("e")
	public TimeUnit e;

	@Named("f1")
	public TimeUnit f1;

	@Named("f2")
	public TimeUnit f2;

	@Named("intNoMinMax")
	public int g1;

	@Named("intMinMax")
	@Min(-1)
	@Max(+2)
	public int g2;

	@Named("longNoMinMax")
	public long g3;

	@Named("longMinMax")
	@Min(-1)
	@Max(+2)
	public long g4;

	@Named("doubleNoMinMax")
	public double g5;

	@Min(-1)
	@Max(+2)
	@Named("doubleMinMax")
	public double g6;

	@Named("floatNoMinMax")
	public float g7;

	@Min(-1)
	@Max(+2)
	@Named("floatMinMax")
	public float g8;

	@Named("charNoMinMax")
	public char g9;

	@Min(-1)
	@Max(+2)
	@Named("charMinMax")
	public char g10;

	@Named("byteNoMinMax")
	public byte g11;

	@Min(-1)
	@Max(+2)
	@Named("byteMinMax")
	public byte g12;

	@Positive
	@Max(+2)
	@Named("positiveAnnotated")
	public byte h1;

	@PositiveOrZero
	@Max(+2)
	@Named("positiveOrZeroAnnotated")
	public byte h2;

	@Min(-2)
	@Negative
	@Named("negativeAnnotated")
	public byte h3;

	@Min(-2)
	@NegativeOrZero
	@Named("negativeOrZeroAnnotated")
	public byte h4;

	@Named("i1")
	public String i1 = "nullMe";

	@Named("i2")
	public TimeUnit i2 = DAYS;

	@Named("i3")
	public int i3 = 42;
	
	@Named("i4")
	public Integer i4 = 42;
	
	public static final ThreadLocal<String[]> choiceValuesOfD = ThreadLocal.withInitial(() -> new String[] { "---unconfigured---" });
	public static final ThreadLocal<Boolean> doDisableXXX = ThreadLocal.withInitial(() -> Boolean.TRUE);

	@Named("a")
	public void setPort(String a) {
		this.a = a;
	}

	@Named("b")
	@Min(3)
	@Max(12)
	public void theNameOfTheSetterDoesNotMatter(int b) {
		this.b = b;
	}

	@Named("proto")
	public void setProtocol(String protocol) {
		this.protocol = protoByName(protocol);
	}

	@ChoiceFor("a")
	public String[] choiceValuesForAtttribute_A() {
		return new String[] { "aVal1", "aVal2" };
	}

	@ChoiceFor("proto")
	public static String[] choiceValuesForAtttribute_proto_typeIsArray() {
		return protocolNames().stream().toArray(String[]::new);
	}

	@ChoiceFor("f1")
	public List<TimeUnit> choiceValuesForAtttribute_f1_typeIsList() {
		return Arrays.asList(NANOSECONDS);
	}

	@ChoiceFor("f2")
	public Stream<TimeUnit> choiceValuesForAtttribute_f2_typeIsStream() {
		return Stream.of(MINUTES, DAYS);
	}

	@Named("a")
	public String getA() {
		return a;
	}

	@Named("b")
	public int getB() {
		return b;
	}

	public String getC() {
		return c;
	}

	@Named("proto")
	public String needAgetterForProtoOfTypeString_thisIsUsedByCacheKey() {
		return protocol == null ? null : protocol.getName();
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	public TimeUnit getF1() {
		return f1;
	}

	public TimeUnit getF2() {
		return f2;
	}

	@ChoiceFor("d")
	public static String[] choiceValuesCanBeSetViaThreadLocalForTesting() {
		return choiceValuesOfD.get();
	}

	@Named(XXX)
	public String xxxDisabled;

	@Override
	public boolean isDisabled(String attributeName) {
		return doDisableXXX.get() && XXX.equals(attributeName);
	}

}