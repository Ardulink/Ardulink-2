/*
Copyright 2013 project Ardulink http://www.ardulink.org/

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
#include <EEPROM.h>


/*
Unique ID is suggested by connected PC. Then it is stored and used for ever.
Unique ID is stored in EEPROM at a given address. A magic number (2 bytes) "LZ"
is used to understand if it is already stored or not.
Actually Unique ID is a UUID in the string format (36 bytes instead of just 16, yes it could be better).
https://en.wikipedia.org/wiki/Universally_unique_identifier
So the final length to store the unique id is 38 bytes.
*/
#define UNIQUE_ID_EEPROM_ADDRESS 0
#define UNIQUE_ID_LENGTH 38
#define UNIQUE_ID_MAGIC_NUMBER_HIGH 76
#define UNIQUE_ID_MAGIC_NUMBER_LOW 90

String inputString = "";         // a string to hold incoming data (Ardulink)
boolean stringComplete = false;  // whether the string is complete (Ardulink)
boolean initialized = false;  // whether the hand shake is complete (Ardulink)

// read commands sent with Ardulink and managed in setup func
void readHandShakeCommands(){
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

  if (stringComplete) {
    
    if(inputString.startsWith("alp://")) { // OK is a message I know (Ardulink)
    
      boolean msgRecognized = true;
      String uniqueID = "";
      
      if(inputString.substring(6,10) == "cust") { // Custom Message
        int separatorPosition = inputString.indexOf('/', 11 );
        int messageIdPosition = inputString.indexOf('?', 11 );
        String customCommand = inputString.substring(11,separatorPosition);
        String value = inputString.substring(separatorPosition + 1, messageIdPosition); // suggested uniqueID
        if(customCommand == "getUniqueID") {
        	 uniqueID = getUniqueID(value);
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
        id.replace("\n", "");
        // print the reply
        Serial.print("alp://rply/");
        if(msgRecognized) { // this sketch doesn't know other messages in this case command is ko (not ok)
          Serial.print("ok?id=");
        } else {
          Serial.print("ko?id=");
        }
        Serial.print(id);
        if(uniqueID.length() > 0) {
        	Serial.print("&UniqueID=");
        	Serial.print(uniqueID);
        	initialized = true;
        }
        Serial.print('\n'); // End of Message
        Serial.flush();
      }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }

}

String getUniqueID(String suggested) {

	char buffer[UNIQUE_ID_LENGTH + 1];
	String retvalue = suggested;
	
	EEPROM.get( UNIQUE_ID_EEPROM_ADDRESS, buffer );
	if(buffer[0] == UNIQUE_ID_MAGIC_NUMBER_HIGH && buffer[1] == UNIQUE_ID_MAGIC_NUMBER_LOW) {
		retvalue = String(&buffer[2]);
	} else {
		buffer[0] = UNIQUE_ID_MAGIC_NUMBER_HIGH;
		buffer[1] = UNIQUE_ID_MAGIC_NUMBER_LOW;
		suggested.toCharArray(&buffer[2], UNIQUE_ID_LENGTH - 1);
		EEPROM.put( UNIQUE_ID_EEPROM_ADDRESS, buffer );
	}

	return retvalue;
}

void setup(){
  Serial.begin(115200);
  while(!Serial); // Wait until Serial not connected (because difference between Leonardo and Micro with UNO and others)

  while(!initialized) {
    readHandShakeCommands();
  }
}

void loop(){
}

