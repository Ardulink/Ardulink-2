package org.ardulink.core.serial.jssc.connectionmanager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.events.CustomEvent;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class RealConnectionListenTest {
	
	private Link link;

	@Before
	public void setup() {
		Configurer configurer = LinkManager.getInstance().getConfigurer(URIs.newURI("ardulink://serial-jssc/"));

		ConfigAttribute portAttribute = configurer.getAttribute("port");
		System.out.println(portAttribute.getChoiceValues()[0]);
		portAttribute.setValue(portAttribute.getChoiceValues()[0]);

		ConfigAttribute pingprobeAttribute = configurer.getAttribute("pingprobe");
		pingprobeAttribute.setValue(false);
		
		link = configurer.newLink();
	}
	
	@Test
	public void listen() throws IOException, InterruptedException {
		
		link.addCustomListener(new CustomListener() {
			
			@Override
			public void customEventReceived(CustomEvent e) {
				System.out.println(e.getMessage());
			}
		});

		TimeUnit.SECONDS.sleep(60);
	}

}

/* this is the sketch you can use
 * 

String inputString = "";         // a string to hold incoming data (Ardulink)
boolean stringComplete = false;  // whether the string is complete (Ardulink)

// print data to serial port 
void dataPrint(unsigned long Count, int Temperature){
  Serial.print("alp://cevnt/");
  Serial.print("RAWMONITOR");
  Serial.print(Count);
  Serial.print("_");
  Serial.print(Temperature);
  Serial.print('\n');
  Serial.flush();
}

// QCM frequency by counting the number of pulses in a fixed time 
unsigned long frequency = 0;
// thermistor temperature
int temperature = 0;

void setup(){
  Serial.begin(115200);
  while(!Serial); // Wait until Serial not connected (because difference between Leonardo and Micro with UNO and others)
}

void loop(){
  frequency = 20;       // measure QCM frequency
  temperature = 10;     // measure temperature 
  dataPrint(frequency, temperature);  // print data
  delay(1000);
}

 *
 *
 */
