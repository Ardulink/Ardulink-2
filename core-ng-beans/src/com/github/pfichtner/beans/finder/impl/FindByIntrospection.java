package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ExecReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.ExecWriteMethod.isWriteMethod;
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
		List<ExecReadMethod> readers = new ArrayList<ExecReadMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isReadMethod(pd.getReadMethod())) {
				readers.add(new ExecReadMethod(bean, pd.getName(), pd
						.getReadMethod()));
			}
		}
		return readers;
	}

	@Override
	public Iterable<? extends AttributeWriter> listWriters(Object bean)
			throws Exception {
		List<ExecWriteMethod> writers = new ArrayList<ExecWriteMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isWriteMethod(pd.getWriteMethod())) {
				writers.add(new ExecWriteMethod(bean, pd.getName(), pd
						.getWriteMethod()));
			}
		}
		return writers;
	}

}