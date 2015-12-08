package com.github.pfichtner.ardulink.core.linkmanager;

import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.I18n;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;

@I18n("message")
public class DummyLinkConfig implements LinkConfig {

	public String a;
	public int b;
	@Named("c")
	public String c;
	public Protocol protocol;

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

}