package com.github.pfichtner.beans.finder.api;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;

public interface AttributeFinder {

	AttributeReader findReader(Object bean, String name) throws Exception;

	AttributeWriter findWriter(Object bean, String name) throws Exception;

}
