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

This sketch is an example to understand how Digispark/PicoDuino can recognize ALProtocol. 
However, it can easily be reused for your own purposes or as a base for a library. 

This sketch is for Digispark/PicoDuino that doesn't support Serial library. Actually it manages
SimpleBynaryProtocol (without responses from Digispark) but just for PIN Power Switch messages.
If you have different needs you have to modify this sketch accordingly.

Remember: Digispark/PicoDuino has just 6.012 bytes memory available for sketches.

*/

#include <DigiUSB.h>

String inputString = "";

void setup() {
  DigiUSB.begin();
}

void get_input() {
  char lastRead;
  // when there are no characters to read, or the character isn't a newline
  while (true) { // loop forever
    if (DigiUSB.available()) {
      // something to read
      lastRead = (char)DigiUSB.read();
      
      if (lastRead == '\n') {
        break; // when we get a newline, break out of loop
      } else {
        // add it to the inputString:
        inputString += lastRead;
      }
    }
    
    // refresh the usb port for 10 milliseconds
    DigiUSB.delay(10);
  }
}

void loop() {
  
  get_input();
  
    if(inputString.startsWith("alp://")) { // OK is a message I know (this is general code you can reuse)
      
      if(inputString.substring(6,10) == "ppsw") { // Power Pin Switch (this is general code you can reuse)
        int separatorPosition = inputString.indexOf('/', 11 );
        String pin = inputString.substring(11,separatorPosition);
        String power = inputString.substring(separatorPosition + 1);
        int pinInt = pin.toInt();
        pinMode(pinInt, OUTPUT);
        if(power.toInt() == 1) {
          digitalWrite(pinInt, HIGH);
        } else {
          digitalWrite(pinInt, LOW);
        }
//      } else 
//      if(inputString.substring(6,10) == "ppin") { // Power Pin Intensity (this is general code you can reuse)
//          int separatorPosition = inputString.indexOf('/', 11 );
//          String pin = inputString.substring(11,separatorPosition);
//          String intens = inputString.substring(separatorPosition + 1);
//          int pinInt = pin.toInt();
//          pinMode(pinInt, OUTPUT);
//          analogWrite(pinInt, intens.toInt());
      }
    
   } 

  inputString = "";
}
