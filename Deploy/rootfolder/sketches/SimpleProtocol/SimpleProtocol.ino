/*
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

This sketch is an example to understand how Digispark/PicoDuino can recognize SimpleBynaryProtocol. 
However, it can easily be reused for your own purposes or as a base for a library. 

This sketch is for Digispark/PicoDuino that doesn't support Serial library. Actually it manages
SimpleBynaryProtocol (without responses from Digispark) but just for PIN Power Switch messages.
If you have different needs you have to modify this sketch accordingly.

Remember: Digispark/PicoDuino has just 6.012 bytes memory available for sketches.

*/

#include <DigiUSB.h>

#define POWER_PIN_INTENSITY_MESSAGE 11
#define POWER_PIN_SWITCH_MESSAGE 12
#define digitalPinListeningNum 14 // Change 14 if you have a different number of pins.

byte inputMessage[10];
byte position = 0;

void setup() {
  // initialize serial: (this is general code you can reuse)
  Serial.begin(115200);

  // Turn off everything (not on RXTX)
  int index = 0;
  for (index = 2; index < digitalPinListeningNum; index++) {
    pinMode(index, OUTPUT);
    digitalWrite(index, LOW);
  }
  
}

void get_input() {
  position = 0;
  byte lastRead;
  // when there are no characters to read, or the character isn't a newline
  while (true) { // loop forever
    if (Serial.available()) {
      // something to read
      lastRead = Serial.read();
      if (lastRead == 255) {
        break; // when we get a divider message, break out of loop
      } else {
        // add it to the inputString:
        inputMessage[position] = lastRead;
        position++;
      }
    }
    
  }
}

void loop() {
  
  get_input();
        
  if(inputMessage[0] == POWER_PIN_SWITCH_MESSAGE) { // Power Pin Switch (this is general code you can reuse)
     digitalWrite(inputMessage[1], inputMessage[2]);
  } else if(inputMessage[0] == POWER_PIN_INTENSITY_MESSAGE) { // Power Pin Intensity (this is general code you can reuse)
      analogWrite(inputMessage[1], inputMessage[2]);          
  }
}
