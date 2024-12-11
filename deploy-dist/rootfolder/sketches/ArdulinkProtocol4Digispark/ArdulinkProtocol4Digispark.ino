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
SimpleBinaryProtocol (without responses from Digispark) but just for PIN Power Switch messages.
If you have different needs you have to modify this sketch accordingly.

Remember: Digispark/PicoDuino has just 6.012 bytes memory available for sketches.

*/

#include <DigiUSB.h>

String inputString = "";

#define UNLIMITED_LENGTH ((size_t)-1)

struct CommandHandler {
    const char* command;
    bool (*handler)(const char* params, size_t length);
};

bool handlePpin(const char* cParams, size_t length) {
  const char* separator = strchr(cParams, '/');
  if (!separator) return false;
  int pin = atoi(cParams);
  int value = atoi(separator + 1);
  pinMode(pin, OUTPUT);
  analogWrite(pin, value);
  return true;
}

bool handlePpsw(const char* cParams, size_t length) {
  const char* separator = strchr(cParams, '/');
  if (!separator) return false;
  int pin = atoi(cParams);
  int value = atoi(separator + 1);
  pinMode(pin, OUTPUT);
  digitalWrite(pin, value == 1 ? HIGH : LOW);
  return true;
}

const CommandHandler commandHandlers[] = {
    {"ppin", handlePpin},
    {"ppsw", handlePpsw}
};

void setup() {
  DigiUSB.begin();
}

void readSerial() {
  char lastRead;
  // when there are no characters to read, or the character isn't a newline
  while (true) { // loop forever
    if (DigiUSB.available()) {
      // something to read
      lastRead = (char) DigiUSB.read();
      
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
  readSerial();

  const char* inputCStr = inputString.c_str();
  
  if (strncmp(inputCStr, "alp://", 6) == 0) {
    const char* commandAndParams = inputCStr + 6; // Skip "alp://"
    const char* idPositionPtr = strstr(commandAndParams, "?id=");

    bool ok = false;
    for (const auto& handler : commandHandlers) {
      int commandLength = strlen(handler.command);
      if (strncmp(commandAndParams, handler.command, commandLength) == 0) {
        const char* paramsStart = commandAndParams + commandLength + 1; // Skip command name
        ok = handler.handler(paramsStart, idPositionPtr ? idPositionPtr - paramsStart : UNLIMITED_LENGTH);
        break;
      }
    }

    if (idPositionPtr) {
      int id = idPositionPtr ? atoi(idPositionPtr + 4) : -1; // Skip "?id=" part
      sendRply(id, ok);
    }
  }

  inputString = "";
}
