package com.github.pfichtner.ardulink.core.proto.api;

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
		Class<?>[] existingInterfaces = delegateTo.getClass().getInterfaces();
		Class<?>[] newInterfaces = new Class<?>[existingInterfaces.length + 1];
		newInterfaces[0] = MessageIdHolder.class;
		System.arraycopy(existingInterfaces, 0, newInterfaces, 1,
				existingInterfaces.length);
		@SuppressWarnings("unchecked")
		T proxy = (T) Proxy.newProxyInstance(delegateTo.getClass()
				.getClassLoader(), newInterfaces,
				new MessageIdHolderInvocationHandler(delegateTo, messageId));
		return proxy;
	}
}