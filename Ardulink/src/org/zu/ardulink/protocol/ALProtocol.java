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

package org.zu.ardulink.protocol;

import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import org.zu.ardulink.Link;
import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.IncomingMessageEvent;


/**
 * [ardulinktitle] [ardulinkversion]
 * This class implements the native Arduino Link protocol.<br/>
 * With this class you are able to send messages to Arduino.<br/>
 * <br/>
 * Message are in the format:<br/>
 * <br/>
 * alp://&lt;request or response&gt;/&lt;variable data&gt;?id=&lt;numeric message id&gt;<br/>
 * <br/>
 * where<br/>
 * requests from ardulink to arduino are: <br/>
 * kprs - Key Pressed<br/>
 * ppin - Power Pin Intensity<br/>
 * ppsw - Power Pin Switch<br/>
 * srld - Start Listening Digital Pin<br/>
 * spld - Stop Listening Digital Pin<br/>
 * srla - Start Listening Analog Pin<br/>
 * spla - Stop Listening Analog Pin<br/>
 * <br/>
 * requests from arduino to ardulink are:<br/>
 * ared - Analog Pin Read<br/>
 * dred - Digital Pin Read<br/>
 * <br/>
 * responses (only from arduino) are:<br/>
 * rply - reply message<br/>
 * <br/>
 * ?id=&lt;numeric message id&gt; is not mandatory (for requests). If is supplied then a asynchronous<br/>
 * rply response will send from arduino. Otherwise arduino will not send a response.<br/>
 * <br/>
 * Each message from ardulink to arduino terminate with a \n<br/>
 * <br/>
 * See methods about variable data.<br/>
 * <br/>
 * Variable data:<br/>
 * alp://kprs/chr&lt;char pressed&gt;cod&lt;key code&gt;loc&lt;key location&gt;mod&lt;key modifiers&gt;mex&lt;key modifiers&gt;?id=&lt;message id&gt;<br/>
 * alp://ppin/&lt;pin&gt;/&lt;intensity&gt;?id=&lt;message id&gt;      intensity:0-255<br/>
 * alp://ppsw/&lt;pin&gt;/&lt;power&gt;?id=&lt;message id&gt;          power:0-1<br/>
 * alp://srld/&lt;pin&gt;?id=&lt;message id&gt;<br/>
 * alp://spld/&lt;pin&gt;?id=&lt;message id&gt;<br/>
 * alp://srla/&lt;pin&gt;?id=&lt;message id&gt;<br/>
 * alp://spla/&lt;pin&gt;?id=&lt;message id&gt;<br/>
 * alp://ared/&lt;pin&gt;/&lt;intensity&gt;                      intensity:0-1023<br/>
 * alp://dred/&lt;pin&gt;/&lt;power&gt;                          power:0-1<br/>
 * alp://rply/ok|ko?id=&lt;message id&gt;<br/>
 * <br/>
 * @author Luciano Zu
 * 
 * [adsense]
 */
public class ALProtocol implements IProtocol {

	public static final String NAME = "ArdulinkProtocol";
	
	private static Logger logger = Logger.getLogger(ALProtocol.class.getName());
	private static long nextId = 1;
	
	private Map<Long, MessageInfo> messageInfos = new Hashtable<Long, MessageInfo>();

	@Override
	public String getProtocolName() {
		return NAME;
	}

	@Override
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex) {
		return sendKeyPressEvent(link, keychar, keycode, keylocation, keymodifiers, keymodifiersex, null);
	}

	@Override
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity) {
		return sendPowerPinIntensity(link, pin, intensity, null);
	}

	@Override
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power) {
		return sendPowerPinSwitch(link, pin, power, null);
	}

	@Override
	public MessageInfo sendCustomMessage(Link link, String message) {
		return sendCustomMessage(link, message, null);
	}
	
	@Override
	public MessageInfo sendKeyPressEvent(Link link, char keychar, int keycode,	int keylocation, int keymodifiers, int keymodifiersex, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}
				
				StringBuilder builder = new StringBuilder("alp://kprs/chr");
				builder.append(keychar);
				builder.append("cod");
				builder.append(keycode);
				builder.append("loc");
				builder.append(keylocation);
				builder.append("mod");
				builder.append(keymodifiers);
				builder.append("mex");
				builder.append(keymodifiersex);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);
				
				if(!result) {
					messageInfos.remove(currentId);
				}
			}
			
		}
		return retvalue;
	}

	@Override
	public MessageInfo sendPowerPinIntensity(Link link, int pin, int intensity, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}
				
				StringBuilder builder = new StringBuilder("alp://ppin/");
				builder.append(pin);
				builder.append("/");
				builder.append(intensity);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		
		return retvalue;
	}

	@Override
	public MessageInfo sendPowerPinSwitch(Link link, int pin, int power, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected() && (power == POWER_HIGH || power == POWER_LOW)) {
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}

				StringBuilder builder = new StringBuilder("alp://ppsw/");
				builder.append(pin);
				builder.append("/");
				builder.append(power);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		return retvalue;
	}

	@Override
	public MessageInfo sendCustomMessage(Link link, String message, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}
				
				StringBuilder builder = new StringBuilder("alp://cust/");
				builder.append(message);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		
		return retvalue;
	}	
	
	@Override
	public MessageInfo startListenDigitalPin(Link link, int pin) {
		return startListenDigitalPin(link, pin, null);
	}

	@Override
	public MessageInfo stopListenDigitalPin(Link link, int pin) {
		return stopListenDigitalPin(link, pin, null);
	}

	@Override
	public MessageInfo startListenAnalogPin(Link link, int pin) {
		return startListenAnalogPin(link, pin, null);
	}

	@Override
	public MessageInfo stopListenAnalogPin(Link link, int pin) {
		return stopListenAnalogPin(link, pin, null);
	}

	@Override
	public MessageInfo startListenDigitalPin(Link link, int pin, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}

				StringBuilder builder = new StringBuilder("alp://srld/");
				builder.append(pin);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		return retvalue;
	}

	@Override
	public MessageInfo stopListenDigitalPin(Link link, int pin, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}

				StringBuilder builder = new StringBuilder("alp://spld/");
				builder.append(pin);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		return retvalue;
	}

	@Override
	public MessageInfo startListenAnalogPin(Link link, int pin, ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}

				StringBuilder builder = new StringBuilder("alp://srla/");
				builder.append(pin);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		return retvalue;
	}

	@Override
	public MessageInfo stopListenAnalogPin(Link link, int pin,	ReplyMessageCallback callback) {
		MessageInfo retvalue = new MessageInfo();
		synchronized(this) {
			if(link.isConnected()) {
				long currentId = nextId++;
				retvalue.setMessageID(currentId);
				if(callback != null) {
					retvalue.setCallback(callback);
					messageInfos.put(currentId, retvalue);
				}

				StringBuilder builder = new StringBuilder("alp://spla/");
				builder.append(pin);
				if(callback != null) {
					builder.append("?id=");
					builder.append(currentId);
				}
				builder.append("\n");
				
				String mesg = builder.toString();
				logger.fine(mesg);
				
				boolean result = link.writeSerial(mesg);
				retvalue.setSent(result);
				retvalue.setMessageSent(mesg);

				if(!result) {
					messageInfos.remove(currentId);
				}
			}
		}
		return retvalue;
	}	
	
	@Override
	public IncomingMessageEvent parseMessage(int[] message) {
		IncomingMessageEvent retvalue = null;
		try {
			String msg = new String(message, 0, message.length).trim();
			if(msg.startsWith("alp://")) { // OK is a message I know
				String cmd = msg.substring(6,10);
				if("rply".equals(cmd)) { // alp://rply/ok?id<messageid> alp://rply/ko?id<messageid>
					parseReplyMessage(msg);
				} else if("dred".equals(cmd)) { // alp://dred/<pin>/<value>
					retvalue = parseDigitalReadMessage(msg);
				} else if("ared".equals(cmd)) { // alp://dred/<pin>/<value>
					retvalue = parseAnalogReadMessage(msg);
				} else { // Message I don't recognize its very strange!
					logger.severe("Arduino sent to me a message in ALProtocol that I don't recognize. Msg: " + msg);
				}
			}
		}
		catch(Exception e) {
			logger.severe("Errror parsing message sent from Arduino. " + e.getMessage());
			e.printStackTrace();
		}
		return retvalue;
	}

	private IncomingMessageEvent parseDigitalReadMessage(String msg) {
        int separatorPosition = msg.indexOf('/', 11 );
        String pin = msg.substring(11,separatorPosition);
        String value = msg.substring(separatorPosition + 1);
        DigitalReadChangeEvent e = new DigitalReadChangeEvent(Integer.parseInt(pin), Integer.parseInt(value), msg);
        return e;
	}

	private IncomingMessageEvent parseAnalogReadMessage(String msg) {
        int separatorPosition = msg.indexOf('/', 11 );
        String pin = msg.substring(11,separatorPosition);
        String value = msg.substring(separatorPosition + 1);
        AnalogReadChangeEvent e = new AnalogReadChangeEvent(Integer.parseInt(pin), Integer.parseInt(value), msg);
        return e;
	}

	private void parseReplyMessage(String msg) {
		String result = msg.substring(11,13);
		int idIndex = msg.indexOf("?id=");
		String tmpId = msg.substring(idIndex + 4).trim();
		Long id = Long.parseLong(tmpId);
		MessageInfo messageInfo = messageInfos.get(id);
		if(messageInfo != null) {
			if("ok".equals(result)) {
				messageInfo.setReply(REPLY_OK);
			} else if("ko".equals(result)) {
				messageInfo.setReply(REPLY_KO);
			}
			messageInfo.setMessageReceived(msg);
			messageInfos.remove(id);
			
			messageInfo.getCallback().replyInfo(messageInfo); // Callback!
		}
	}

}
