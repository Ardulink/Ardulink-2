package org.zu.ardulink.connection.pi;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zu.ardulink.ConnectionContact;
import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.protocol.ALProtocol;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.parser.IProtocolMessageStore;
import org.zu.ardulink.protocol.parser.IProtocolParser;
import org.zu.ardulink.protocol.parser.MessageParsedInfo;
import org.zu.ardulink.protocol.parser.MessageType;
import org.zu.ardulink.protocol.parser.ParseException;
import org.zu.ardulink.protocol.parser.ProtocolParserHandler;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.PinEventType;

public class RaspberryPIConnection implements Connection {
	
	public static final String CONNECTION_NAME = "Raspberry PI";

	private ConnectionContact connectionContact;
	
	/**
	 * The status of the connection.
	 */
	private boolean connected;

	private GpioController gpioController;
	
	/**
	 * The protocol used by the link instance for this connection
	 */
	private IProtocolParser protocolParser;
	private IProtocolMessageStore messageStore;
	
	/**
	 * Listeners
	 */
	private Map<Integer, GpioPinListenerDigital> digitalListeners = new HashMap<Integer, GpioPinListenerDigital>();
	private Map<Integer, GpioPinListenerAnalog>  analogListeners  = new HashMap<Integer, GpioPinListenerAnalog>();
	
	public RaspberryPIConnection() {
		this(ALProtocol.NAME);
	}

	public RaspberryPIConnection(String protocolName) {
		super();
		protocolParser = ProtocolParserHandler.getProtocolParserImplementation(protocolName);
		checkNotNull(protocolParser, "Protocol not supported. Resiter the right parser for: " + protocolName);

		messageStore = protocolParser.getMessageStore();
	}

	
	
	@Override
	public List<String> getPortList() {
		return Collections.singletonList(CONNECTION_NAME);
	}

	public boolean connect() {
		if(!connected) {
			gpioController = GpioFactory.getInstance();
			if(gpioController != null) {
				connected = true;
				if(connectionContact != null) {
					connectionContact.connected(CONNECTION_NAME, CONNECTION_NAME);
				}
			}
		}
		return connected;
	}
	
	@Override
	public boolean connect(Object... params) {
		checkNotNull(params, "Params must be null");
		return connect();
	}

	@Override
	public boolean disconnect() {
		if(connected) {
			gpioController.shutdown();
			gpioController = null;
			connected = false;
			if(connectionContact != null) {
				connectionContact.disconnected(CONNECTION_NAME);
			}
		}
		return !connected;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean writeSerial(String message) {
		boolean success = false;
		if (isConnected()) {
			try {
				success = true;
				messageStore.addMessageChunck(message);
				if(messageStore.isMessageComplete()) {
					MessageParsedInfo messageParsedInfo = protocolParser.parse(messageStore.getNextMessage());
					success = processMessage(messageParsedInfo);
					if(messageParsedInfo.getId() != IProtocol.UNDEFINED_ID) {
						reply(success, messageParsedInfo);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				disconnect();
			}
		} else {
			connectionContact.writeLog(CONNECTION_NAME, "No port is connected.");
		}
		return success;
	}

	private void reply(final boolean success, final MessageParsedInfo messageParsedInfo) {
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int[] reply = protocolParser.reply(success, messageParsedInfo);
				connectionContact.parseInput(CONNECTION_NAME, reply.length, reply);
			}
		});

		thread.start();
	}

	@Override
	public boolean writeSerial(int numBytes, int[] message) {

		throw new UnsupportedOperationException("Please use: writeSerial(String message) instead.");
	}

	@Override
	public void setConnectionContact(ConnectionContact contact) {
		connectionContact = contact;
	}
	
	public void writeLog(String text) {
		connectionContact.writeLog(CONNECTION_NAME, text);
	}

	private boolean processMessage(MessageParsedInfo messageParsedInfo) {
		boolean retvalue = false;
		
		if(MessageType.PPSW == messageParsedInfo.getMessageType()) {
			
			retvalue = sendPowerPinSwitch(messageParsedInfo);
			
		} else if(MessageType.PPIN == messageParsedInfo.getMessageType()) {
			
			retvalue = sendPowerPinIntensity(messageParsedInfo);
			
		} else if(MessageType.KPRS == messageParsedInfo.getMessageType()) {
			
			throw new UnsupportedOperationException("This connection doesn't support key press messages");
			
		} else if(MessageType.TONE == messageParsedInfo.getMessageType()) {

			throw new UnsupportedOperationException("This connection doesn't support tone messages");
			
		} else if(MessageType.NOTN == messageParsedInfo.getMessageType()) {
			
			throw new UnsupportedOperationException("This connection doesn't support notone messages");
			
		} else if(MessageType.SRLD == messageParsedInfo.getMessageType()) {

			retvalue = startListenDigitalPin(messageParsedInfo);
						
		} else if(MessageType.SPLD == messageParsedInfo.getMessageType()) {
			
			retvalue = stopListenDigitalPin(messageParsedInfo);
			
		} else if(MessageType.SRLA == messageParsedInfo.getMessageType()) {
			
			retvalue = startListenAnalogPin(messageParsedInfo);
			
		} else if(MessageType.SPLA == messageParsedInfo.getMessageType()) {
			
			retvalue = stopListenAnalogPin(messageParsedInfo);
			
		} else if(MessageType.CUST == messageParsedInfo.getMessageType()) {
			
			throw new UnsupportedOperationException("This connection doesn't support custom messages");
			
		} else if(MessageType.ARED == messageParsedInfo.getMessageType()) {
			
			throw new IllegalStateException("Analog Read Event Message shoudn't come here");

		} else if(MessageType.DRED == messageParsedInfo.getMessageType()) {
			
			throw new IllegalStateException("Digital Read Event Message shoudn't come here");

		} else if(MessageType.RPLY == messageParsedInfo.getMessageType()) {
			
			throw new IllegalStateException("Reply Event Message shoudn't come here");

		}

		return retvalue;
	}

	private boolean sendPowerPinSwitch(MessageParsedInfo messageParsedInfo) {
		
		int pinNum = (Integer)messageParsedInfo.getParsedValues()[0];
		int power  = (Integer)messageParsedInfo.getParsedValues()[1];
				
		GpioPinDigitalOutput pin = (GpioPinDigitalOutput)getPin(pinNum);
		pin.setMode(PinMode.DIGITAL_OUTPUT);
		
		if(power == IProtocol.LOW) {
			pin.low();
		} else {
			pin.high();
		}
		
		return true;
	}

	private boolean sendPowerPinIntensity(MessageParsedInfo messageParsedInfo) {
		int pinNum    = (Integer)messageParsedInfo.getParsedValues()[0];
		int intensity = (Integer)messageParsedInfo.getParsedValues()[1];
				
		GpioPinPwmOutput  pin = (GpioPinPwmOutput )getPin(pinNum);
		pin.setMode(PinMode.PWM_OUTPUT);
		
		pin.setPwm(intensity);
				
		return true;
	}

	private boolean startListenDigitalPin(MessageParsedInfo messageParsedInfo) {
		int pinNum    = (Integer)messageParsedInfo.getParsedValues()[0];
		
		GpioPinListenerDigital listener = digitalListeners.get(pinNum);
		if(listener == null) {
			listener = createDigitalListener(pinNum);
			GpioPinDigitalInput pin = (GpioPinDigitalInput)getPin(pinNum);
			pin.setMode(PinMode.DIGITAL_INPUT);
			pin.setPullResistance(PinPullResistance.PULL_DOWN);
			
			digitalListeners.put(pinNum, listener);
			pin.addListener(listener);
		}
		
		return true;
	}

	private boolean stopListenDigitalPin(MessageParsedInfo messageParsedInfo) {
		int pinNum    = (Integer)messageParsedInfo.getParsedValues()[0];
		GpioPinListenerDigital listener = digitalListeners.get(pinNum);
		if(listener != null) {
			GpioPinDigitalInput pin = (GpioPinDigitalInput)getPin(pinNum);
			
			digitalListeners.remove(pin);
			pin.removeListener(listener);
		}
		
		return true;
	}

	private boolean startListenAnalogPin(MessageParsedInfo messageParsedInfo) {
		int pinNum    = (Integer)messageParsedInfo.getParsedValues()[0];
		
		GpioPinListenerAnalog listener = analogListeners.get(pinNum);
		if(listener == null) {
			listener = createAnalogListener(pinNum);
			GpioPinAnalogInput pin = (GpioPinAnalogInput)getPin(pinNum);
			pin.setMode(PinMode.ANALOG_INPUT);
			pin.setPullResistance(PinPullResistance.PULL_DOWN);
			
			analogListeners.put(pinNum, listener);
			pin.addListener(listener);
		}
		
		return true;
	}

	private boolean stopListenAnalogPin(MessageParsedInfo messageParsedInfo) {
		int pinNum    = (Integer)messageParsedInfo.getParsedValues()[0];
		GpioPinListenerAnalog listener = analogListeners.get(pinNum);
		if(listener != null) {
			GpioPinAnalogInput pin = (GpioPinAnalogInput)getPin(pinNum);
			
			analogListeners.remove(pin);
			pin.removeListener(listener);
		}
		
		return true;
	}

	private GpioPin getPin(int address) {

		GpioPin retvalue = null;
		
		Collection<GpioPin> provisioned = gpioController.getProvisionedPins();
		for (GpioPin gpioPin : provisioned) {
			if(gpioPin.getPin().getAddress() == address) {
				retvalue = gpioPin;
				break;
			}
		}
		
		if(retvalue == null) {
			retvalue = gpioController.provisionPin(RaspiPin.getPinByName("GPIO " + address), PinMode.DIGITAL_OUTPUT);
		}
		
		return retvalue;
	}

	private GpioPinListenerDigital createDigitalListener(int pinNum) {
		return new DigitalListener(pinNum);
	}

	private GpioPinListenerAnalog createAnalogListener(int pinNum) {
		return new AnalogListener(pinNum);
	}

	class DigitalListener implements GpioPinListenerDigital {
		
		private int pin;
		
		public DigitalListener(int pin) {
			super();
			this.pin = pin;
		}

		@Override
		public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
			if(event.getEventType() == PinEventType.DIGITAL_STATE_CHANGE) {
				int[] message;
				if(event.getState().isHigh()) {
					message = protocolParser.digitalRead(pin, IProtocol.HIGH);
				} else {
					message = protocolParser.digitalRead(pin, IProtocol.LOW);
				}
				
				connectionContact.parseInput(CONNECTION_NAME, message.length, message);
			}
		}
	}

	class AnalogListener implements GpioPinListenerAnalog {
		
		private int pin;
		
		public AnalogListener(int pin) {
			super();
			this.pin = pin;
		}

		@Override
		public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
			if(event.getEventType() == PinEventType.ANALOG_VALUE_CHANGE) {
				int[] message = protocolParser.analogRead(pin, (int)event.getValue());
				
				connectionContact.parseInput(CONNECTION_NAME, message.length, message);
			}
		}
	}
}
