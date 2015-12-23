package org.zu.ardulink.protocol.parser;

import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.util.Primitive;

import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkArgument;

public class MessageParsedInfo {
	
	private String message;
	private MessageType messageType;
	private int id = IProtocol.UNDEFINED_ID;
	private String[] values;
	private Object[] parsedValues;

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String[] getValues() {
		return values;
	}
	public void setValues(String[] values) throws ParseException {
		
		checkNotNull(messageType, "setValues is allowed only after setMessageType method invoke");
		
		this.values = values;
		validateValues();
	}

	public Object[] getParsedValues() {
		return parsedValues;
	}

	private void validateValues() throws ParseException {
		
		try {
			if(MessageType.PPSW == messageType) {
				
				checkArgument(values.length == 2, "Values in the message for power pin swith has to be two. Values are: %s", values.length);
				
				parsedValues = new Object[2];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				parsedValues[1] = Primitive.INT.parse(values[1]);
				
				checkArgument(((Integer)parsedValues[1] == IProtocol.HIGH || (Integer)parsedValues[1] == IProtocol.LOW), "POWER PIN accepted values are: %d or %d. Value was: %d", IProtocol.HIGH, IProtocol.LOW, parsedValues[1]);
				
			} else if(MessageType.PPIN == messageType) {
				
				checkArgument(values.length == 2, "Values in the message for power pin intensity has to be two. Values are: %s", values.length);
				
				parsedValues = new Object[2];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				parsedValues[1] = Primitive.INT.parse(values[1]);
				
			} else if(MessageType.KPRS == messageType) {
				
				// TODO!!!!
				
			} else if(MessageType.TONE == messageType) {
				
				checkArgument(values.length == 2, "Values in the message for tone has to be two. Values are: %s", values.length);
				
				parsedValues = new Object[2];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				parsedValues[1] = Primitive.INT.parse(values[1]);
				
			} else if(MessageType.NOTN == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for notone has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				
			} else if(MessageType.SRLD == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for start listen digital has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				
			} else if(MessageType.SPLD == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for stop listen digital has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				
			} else if(MessageType.SRLA == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for start listen analog has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				
			} else if(MessageType.SPLA == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for stop listen analog has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = Primitive.INT.parse(values[0]);
				
			} else if(MessageType.CUST == messageType) {
				
				checkArgument(values.length == 1, "Values in the message for custom has to be just one. Values are: %s", values.length);
				
				parsedValues = new Object[1];
				parsedValues[0] = values[0];
				
			} else if(MessageType.ARED == messageType) {
				
				// TODO

			} else if(MessageType.DRED == messageType) {
				
				// TODO

			} else if(MessageType.RPLY == messageType) {
				
				// TODO

			} else {
				throw new ParseException(messageType + " is an unknown message type");
			}
			
		} catch (NumberFormatException e) {
			throw new ParseException("Message: " + message + " has a value not numeric for a numeric value type.");
		}
	}
}
