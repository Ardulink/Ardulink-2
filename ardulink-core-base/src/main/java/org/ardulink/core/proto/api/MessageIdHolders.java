/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ardulink.core.proto.api;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class MessageIdHolders {

	private MessageIdHolders() {
		super();
	}

	public static class MessageIdHolderInvocationHandler implements
			InvocationHandler {

		private static final Method messageIdHolderGetIdMethod = getMessageIdHolderGetIdMethod();

		private final Object delegate;
		private final Long messageId;

		public MessageIdHolderInvocationHandler(Object delegate, long messageId) {
			this.delegate = delegate;
			this.messageId = Long.valueOf(messageId);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			return isMessageIdHolderGetIdMethod(method) ? messageId : delegate(
					method, args);
		}

		private boolean isMessageIdHolderGetIdMethod(Method method) {
			return messageIdHolderGetIdMethod.equals(method);
		}

		private Object delegate(Method method, Object[] args)
				throws IllegalAccessException, InvocationTargetException {
			return method.invoke(delegate, args);
		}

	}

	private static Method getMessageIdHolderGetIdMethod() {
		try {
			return MessageIdHolder.class.getMethod("getId");
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a dynamic proxy that implements MessageIdHolder automatically.
	 * Calls to {@link MessageIdHolder#getId()} will return the passed
	 * <code>messageId</code>. All other calls are forwarded to the passed
	 * <code>delegateTo</code>.
	 * 
	 * @param delegateTo
	 *            the instance to delegate all calls except
	 *            {@link MessageIdHolder#getId()}
	 * @param messageId
	 *            the messageId to return when {@link MessageIdHolder#getId()}
	 *            is called
	 * @return dynamic proxy implementing {@link MessageIdHolder}
	 */
	@SuppressWarnings("unchecked")
	public static <T> T addMessageId(T delegateTo, long messageId) {
		return (T) Proxy.newProxyInstance(
				delegateTo.getClass().getClassLoader(),
				insertInto(Class.class, MessageIdHolder.class, delegateTo
						.getClass().getInterfaces()),
				new MessageIdHolderInvocationHandler(delegateTo, messageId));
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