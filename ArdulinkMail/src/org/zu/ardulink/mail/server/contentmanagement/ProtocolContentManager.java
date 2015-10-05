/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.mail.server.contentmanagement;

import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;
import static org.zu.ardulink.util.Primitive.parseAs;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.zu.ardulink.Link;
import org.zu.ardulink.mail.server.links.configuration.utils.ConfigurationUtility;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.MessageInfo;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProtocolContentManager implements IContentManager {

	@Override
	public boolean isForContent(String content, List<String> mailContentHooks) {
		for (String hook : mailContentHooks) {
			if(content.toUpperCase().contains(hook.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String execute(String content, List<String> values, List<String> mailContentHooks, List<String> aLinkNames) {
		StringBuilder builder = new StringBuilder();
		
		List<Link> links = ConfigurationUtility.getConnectedLinks(aLinkNames);
		for (Link link : links) {
			for (String value : values) {
				String returnMessage;
				boolean messageSent = false;
				try {
					returnMessage = sendMessage(link, value);
					messageSent = true;
				} catch (Exception e) {
					e.printStackTrace();
					returnMessage = e.getMessage();
				}
				builder.append("message ");
				builder.append(value);
				if(messageSent) {
					builder.append(" sent for link: ");
				} else {
					builder.append(" NOT sent for link: ");
				}
				builder.append(link.getName());
				builder.append(" with this result: ");
				builder.append(returnMessage);
				builder.append("\n");
			}
		}
		
		return builder.toString();
	}

	private String sendMessage(Link link, String message) throws Exception {
		
		MethodAndParameters methodAndParameters = findMethodAndParameters(link, message);
		
		Caller caller = new Caller(link, message, methodAndParameters);
		Thread thread = new Thread(caller);
		thread.start();
		thread.join();

		checkState(caller.isMessageSent(), caller.getResult());

		return caller.getResult();
	}

	private MethodAndParameters findMethodAndParameters(Link link, String message) {
		
		MethodAndParameters retvalue = new MethodAndParameters();

		int indexOfOpenParenthesis = message.indexOf("(");
		int indexOfCloseParenthesis = message.indexOf(")");
		
		checkState(
				indexOfOpenParenthesis != -1,
				"Configuration Exception: in message %s does not exist an open parenthesis",
				message);
		
		checkState(
				indexOfCloseParenthesis != -1,
				"Configuration Exception: in message %s does not exist a close parenthesis",
				message);
		
		String methodName = message.substring(0, indexOfOpenParenthesis);
		String[] parametersAsString = message.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis).split(",");
		
		Method[] allMethods = link.getClass().getMethods();
		boolean found = false;
		for (int i = 0; i < allMethods.length && !found; i++) {
			Method method = allMethods[i];
			// search for a method with a parameter more than one because Callback
			if(method.getName().equals(methodName) && method.getParameterTypes().length + 1 == parametersAsString.length) {
				retvalue.setMethod(method);
				found = true;
			}
		}

		checkState(found, "%s method with %s parameters not found in %s class",
				methodName, parametersAsString.length, link.getClass()
						.getName());
		List<Object> parameters = new LinkedList<Object>();
		Class<?>[] types = retvalue.getMethod().getParameterTypes();
		for (int i = 0; i < parametersAsString.length; i++) {
			parametersAsString[i] = parametersAsString[i].trim();
			parameters.add(getParameter(types[i], parametersAsString[i]));
		}
		retvalue.setParameters(parameters);		
		
		return retvalue;
		
	}

	private Object getParameter(Class<?> parameterType, String value) {
		return checkNotNull(parseAs(parameterType, value),
				"Class: %s is not legal in the %s", parameterType.getName(),
				this.getClass().getName());
	}

	private class MethodAndParameters {
		
		Method method;
		List<Object> parameters = new LinkedList<Object>();
		
		public Method getMethod() {
			return method;
		}
		public void setMethod(Method method) {
			this.method = method;
		}
		public List<Object> getParameters() {
			return parameters;
		}
		public void setParameters(List<Object> parameters) {
			this.parameters = parameters;
		}
		public boolean addParameter(Object parameter) {
			return parameters.add(parameter);
		}
		
	}
	
	private class Caller implements Runnable, ReplyMessageCallback {
		
		private Link link;
		private String message;
		private MethodAndParameters methodAndParameters;
		private String result;
		private boolean messageSent;
		private boolean callbackCalled;
		
		private int counter = 0;
		
		private static final int MAX_COUNT = 20; // Timed out!

		public Caller(Link link, String message, MethodAndParameters methodAndParameters) {
			this.link = link;
			this.message = message;
			this.methodAndParameters = methodAndParameters;
		}

		public String getResult() {
			return result;
		}
		
		public boolean isMessageSent() {
			return messageSent;
		}

		@Override
		public void replyInfo(MessageInfo messageInfo) {
			if(messageInfo.getReply() == IProtocol.REPLY_OK) {
				result = "OK";
			} else {
				result = "ERROR";
			}

			callbackCalled = true;
		}

		@Override
		public void run() {
			methodAndParameters.addParameter(this);
			try {
				
			 methodAndParameters.getMethod().invoke(link, methodAndParameters.getParameters().toArray());
			 
			 while(!callbackCalled && counter > MAX_COUNT) {
				 Thread.sleep(1000);
				 counter++;
			 }
			 
			 checkState(callbackCalled, "Timed out.");
				
			} catch (Exception e) {
				e.printStackTrace();
				result = e.getMessage();
				messageSent = false;
			}
		}
		
	}
}
