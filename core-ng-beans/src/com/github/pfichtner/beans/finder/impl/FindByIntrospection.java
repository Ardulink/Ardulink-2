package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.WriteMethod.isWriteMethod;
import static java.beans.Introspector.getBeanInfo;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;
import com.github.pfichtner.beans.finder.api.AttributeFinder;

public class FindByIntrospection implements AttributeFinder {

	private static final FindByIntrospection instance = new FindByIntrospection();

	public static FindByIntrospection beanAttributes() {
		return instance;
	}

	private FindByIntrospection() {
		super();
	}

	@Override
	public Iterable<? extends AttributeReader> listReaders(Object bean)
			throws Exception {
		List<ReadMethod> readers = new ArrayList<ReadMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isReadMethod(pd.getReadMethod())) {
				readers.add(new ReadMethod(bean, pd.getName(), pd
						.getReadMethod()));
			}
		}
		return readers;
	}

	@Override
	public Iterable<? extends AttributeWriter> listWriters(Object bean)
			throws Exception {
		List<WriteMethod> writers = new ArrayList<WriteMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isWriteMethod(pd.getWriteMethod())) {
				writers.add(new WriteMethod(bean, pd.getName(), pd
						.getWriteMethod()));
			}
		}
		return writers;
	}

}