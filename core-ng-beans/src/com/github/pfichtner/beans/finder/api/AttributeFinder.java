package com.github.pfichtner.beans.finder.api;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;

public interface AttributeFinder {

	Iterable<? extends AttributeReader> listReaders(Object bean)
			throws Exception;

	Iterable<? extends AttributeWriter> listWriters(Object bean)
			throws Exception;

}
