package org.zu.ardulink.protocol.parser;

public interface IProtocolParser {
	
	/**
	 * 
	 * @return the protocol name
	 */
	public String getProtocolName();


	/**
	 * Parse an incoming message.
	 * @param message
	 * @return MessageParsedInfo
	 * @throws ParseException
	 */
	public MessageParsedInfo parse(String message) throws ParseException;


	/**
	 * write a Reply message like arduino that use a specific protocol 
	 * @param success
	 * @param messageParsedInfo
	 * @return
	 */
	public int[] reply(boolean success, MessageParsedInfo messageParsedInfo);
	
	/**
	 * write a message for an analog read event
	 * @param pin
	 * @param value
	 * @return
	 */
	public int[] analogRead(int pin, int value);

	/**
	 * write a message for a digital read event
	 * @param pin
	 * @param value
	 * @return
	 */
	public int[] digitalRead(int pin, int value);

	/**
	 * write a custom message
	 * @param message
	 * @return
	 */
	public int[] customMessage(String message);
	
	public IProtocolMessageStore getMessageStore();
}
