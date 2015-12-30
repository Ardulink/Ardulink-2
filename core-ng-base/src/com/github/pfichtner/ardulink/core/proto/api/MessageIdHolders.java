package com.github.pfichtner.ardulink.core.proto.api;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class MessageIdHolders {

	private MessageIdHolders() {
		super();
	}

	public static class MessageIdHolderInvocationHandler implements
			InvocationHandler {

		private static final Method messageIdHolderGetIdMethod = getMessageIdHolderGetIdMethod();

		private final Object delegate;
		private final long messageId;

		private static Method getMessageIdHolderGetIdMethod() {
			try {
				return MessageIdHolder.class.getMethod("getId");
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}

		public MessageIdHolderInvocationHandler(Object delegate, long messageId) {
			this.delegate = delegate;
			this.messageId = messageId;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return messageIdHolderGetIdMethod.equals(method) ? messageId
					: method.invoke(delegate, args);
		}

	}

	public static <T> T proxy(T delegateTo, long messageId) {
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(
				delegateTo.getClass().getClassLoader(),
				insertInto(Class.class, MessageIdHolder.class, delegateTo
						.getClass().getInterfaces()),
				new MessageIdHolderInvocationHandler(delegateTo, messageId));
		return proxy;
	}

	private static <T> T[] insertInto(Class<T> type, T toAdd, T[] src) {
		T[] newArray = newArray(type, src.length + 1);
		newArray[0] = toAdd;
		System.arraycopy(src, 0, newArray, 1, src.length);
		return newArray;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(Class<T> type, int length) {
		return (T[]) Array.newInstance(type, length);
	}

}