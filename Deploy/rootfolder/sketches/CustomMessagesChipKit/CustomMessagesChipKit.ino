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

This sketch is an example to understand how Arduino can recognize ALProtocol. 
However, it can easily be reused for their own purposes or as a base for a library. 
Read carefully the comments. When you find "this is general code you can reuse"
then it means that it is generic code that you can use to manage the ALProtocol. 
When you find "this is needed just as example for this sketch" then it means that 
you code useful for a specific purpose. In this case you have to modify it to suit 
your needs.
*/

#include <IOShieldOled.h>

#define BUFFER_SIZE 64

char buffer[BUFFER_SIZE];

String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)

void setup() {
  // initialize serial: (this is general code you can reuse)
  Serial.begin(115200);
  
  IOShieldOled.begin();
}

void loop() {

  while (Serial.available() && !stringComplete) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    }
  }

  
  // when a newline arrives:
  if (stringComplete) {
    
    if(inputString.startsWith("alp://")) { // OK is a message I know (this is general code you can reuse)
    
      boolean msgRecognized = true;
      
      if(inputString.substring(6,10) == "cust") { // Custom Message
        // here you can write your own code.
        int separatorPosition = inputString.indexOf('/', 11 );
        int messageIdPosition = inputString.indexOf('?', 11 );
        String customId = inputString.substring(11,separatorPosition);
        String value = inputString.substring(separatorPosition + 1, messageIdPosition);
        if(customId == "OLED") {
            //Clear the virtual buffer
            IOShieldOled.clearBuffer();
            //Chosing Fill pattern 0
            IOShieldOled.setFillPattern(IOShieldOled.getStdPattern(0));
            //Turn automatic updating off
            IOShieldOled.setCharUpdate(0);
            IOShieldOled.setCursor(0, 0);
            value.toCharArray(buffer, BUFFER_SIZE);
            IOShieldOled.putString(buffer);
            IOShieldOled.updateDisplay();
        } else {
          msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
        }
      } else {
        msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
      }
      
      // Prepare reply message if caller supply a message id (this is general code you can reuse)
      int idPosition = inputString.indexOf("?id=");
      if(idPosition != -1) {
        String id = inputString.substring(idPosition + 4);
        // print the reply
        Serial.print("alp://rply/");
        if(msgRecognized) { // this sketch doesn't know other messages in this case command is ko (not ok)
          Serial.print("ok?id=");
        } else {
          Serial.print("ko?id=");
        }
        Serial.print(id);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  
}



