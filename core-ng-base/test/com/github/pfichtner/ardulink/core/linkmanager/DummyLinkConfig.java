package com.github.pfichtner.ardulink.core.linkmanager;

import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.I18n;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;

@I18n("message")
public class DummyLinkConfig implements LinkConfig {

	public String a;
	public int b = 42;
	@Named("c")
	public String c;
	public Protocol protocol;
	@Named("d")
	public String d;
	public static final ThreadLocal<String[]> choiceValuesOfD = new ThreadLocal<String[]>();

	@Named("a")
	public void setPort(String a) {
		this.a = a;
	}

	@Named("b")
	public void theNameOfTheSetterDoesNotMatter(int b) {
		this.b = b;
	}

	@Named("proto")
	public void setProtocol(String protocol) {
		this.protocol = Protocols.getByName(protocol);
	}

	@ChoiceFor("a")
	public String[] choiceValuesForAtttribute_A() {
		return new String[] { "aVal1", "aVal2" };
	}

	@ChoiceFor("proto")
	public static String[] getProtocolsMayAlsoBeStatic() {
		List<String> names = Protocols.list();
		return names.toArray(new String[names.size()]);
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

	public Protocol getProto() {
		return protocol;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}

	@ChoiceFor("d")
	public static String[] choiceValuesCanBeSetViaThreadLocalForTesting() {
		return choiceValuesOfD.get();
	}

}