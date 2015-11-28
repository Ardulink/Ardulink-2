package com.github.pfichtner.beans.finder.impl;

import static com.github.pfichtner.beans.finder.impl.ExecReadMethod.isReadMethod;
import static com.github.pfichtner.beans.finder.impl.ExecWriteMethod.isWriteMethod;
import static java.beans.Introspector.getBeanInfo;

import java.beans.PropertyDescriptor;

import com.github.pfichtner.beans.Attribute.AttributeReader;
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
	public AttributeReader findReader(Object bean, String name)
			throws Exception {
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (name.equals(pd.getName()) && isReadMethod(pd.getReadMethod())) {
				return new ExecReadMethod(bean, pd.getReadMethod());
			}
		}
		return null;
	}

	@Override
	public ExecWriteMethod findWriter(Object bean, String name)
			throws Exception {
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (name.equals(pd.getName()) && isWriteMethod(pd.getWriteMethod())) {
				return new ExecWriteMethod(bean, pd.getWriteMethod());
			}
		}
		return null;
	}

}
