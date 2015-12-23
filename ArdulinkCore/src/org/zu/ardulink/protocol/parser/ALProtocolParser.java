package org.zu.ardulink.protocol.parser;

import static org.zu.ardulink.util.Preconditions.checkArgument;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import org.zu.ardulink.protocol.ALProtocol;
import org.zu.ardulink.util.Strings;

public class ALProtocolParser implements IProtocolParser {
	
	@Override
	public String getProtocolName() {
		return ALProtocol.NAME;
	}

	@Override
	public MessageParsedInfo parse(String message) throws ParseException {

		MessageParsedInfo parsedInfo = new MessageParsedInfo();
		parsedInfo.setMessage(message);
		
		message = parseEnvelope(message);
		parseInnerMessage(message, parsedInfo);

		return parsedInfo;
	}

	private String parseEnvelope(String message) throws ParseException {

		try {
			
			checkNotNull(message, "Parsing message must not be null");
			checkArgument(message.endsWith(Strings.bytes2String(ALProtocol.DEFAULT_OUTGOING_MESSAGE_DIVIDER)), "Parsing message has to finish with the right divider char.");
			checkArgument(message.startsWith("alp://"), "Parsing message has to start with alp://");
						
			message = message.substring(0, message.length() - 1);
			message = message.substring("alp://".length());
			
			return message;
			
		} catch(IllegalStateException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}

	private void parseInnerMessage(String message, MessageParsedInfo parsedInfo) throws ParseException {
		try {
			
			checkArgument(message.length() > 4, "Inner message has to be at least 5 chars: %s", message);
			String strMessageType = message.substring(0, 4);
			MessageType messageType = getMessageType(strMessageType);
			parsedInfo.setMessageType(messageType);
			int idPosition = message.lastIndexOf("?id=");
			if(idPosition != -1) {
				int id = retrieveId(message, idPosition + 4);
				parsedInfo.setId(id);
				message = message.substring(0, idPosition);
			}
			String[] values = message.substring(5).split("/");
			parsedInfo.setValues(values);
		} catch(IllegalStateException e) {
			throw new ParseException(e.getMessage(), e);
		}
	}

	private MessageType getMessageType(String strMessageType) throws ParseException {
		MessageType retvalue = null;
		if(strMessageType.equals("ppsw")) {
			retvalue = MessageType.PPSW;
		} else if(strMessageType.equals("ppin")) {
			retvalue = MessageType.PPIN;
		} else if(strMessageType.equals("kprs")) {
			retvalue = MessageType.KPRS;
		} else if(strMessageType.equals("tone")) {
			retvalue = MessageType.TONE;
		} else if(strMessageType.equals("notn")) {
			retvalue = MessageType.NOTN;
		} else if(strMessageType.equals("srld")) {
			retvalue = MessageType.SRLD;
		} else if(strMessageType.equals("spld")) {
			retvalue = MessageType.SPLD;
		} else if(strMessageType.equals("srla")) {
			retvalue = MessageType.SRLA;
		} else if(strMessageType.equals("spla")) {
			retvalue = MessageType.SPLA;
		} else if(strMessageType.equals("cust")) {
			retvalue = MessageType.CUST;
//		} else if(strMessageType.equals("ared")) {
//			retvalue = MessageType.ARED;
//		} else if(strMessageType.equals("dred")) {
//			retvalue = MessageType.DRED;
//		} else if(strMessageType.equals("rply")) {
//			retvalue = MessageType.RPLY;
		} else {
			throw new ParseException(strMessageType + " is an unknown message type");
		}
		
		return retvalue;
	}

	private int retrieveId(String message, int i) throws ParseException {
		int retvalue = -1;
		
		String idStr = message.substring(i);
		try {
			retvalue = Integer.parseInt(idStr);
		} catch(NumberFormatException e) {
			throw new ParseException("ID is not a number: " + idStr);
		}
		
		return retvalue;
	}

	@Override
	public int[] reply(boolean success, MessageParsedInfo messageParsedInfo) {
		
		StringBuilder builder = new StringBuilder("alp://rply/");
		
		if(success) {
			builder.append("ok");
		} else {
			builder.append("ko");
		}
		
		builder.append("?id=");
		builder.append(messageParsedInfo.getId());
		
		return Strings.string2Ints(builder.toString());
	}

	@Override
	public int[] analogRead(int pin, int value) {
		
		StringBuilder builder = new StringBuilder("alp://ared/");
		builder.append(pin).append("/").append(value);
		return Strings.string2Ints(builder.toString());
	}

	@Override
	public int[] digitalRead(int pin, int value) {
		StringBuilder builder = new StringBuilder("alp://dred/");
		builder.append(pin).append("/").append(value);
		return Strings.string2Ints(builder.toString());
	}

	@Override
	public int[] customMessage(String message) {
		throw new UnsupportedOperationException("NOT SUPPORTED EVENT");
	}

	@Override
	public IProtocolMessageStore getMessageStore() {
		return new ALProtocolMessageStore();
	}

}
