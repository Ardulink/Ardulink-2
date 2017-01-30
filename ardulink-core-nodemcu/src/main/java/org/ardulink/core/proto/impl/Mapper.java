package org.ardulink.core.proto.impl;

import java.util.Map;

public interface Mapper {

	Map<String, String> buildMap(Integer pin, Object[] values);

}
