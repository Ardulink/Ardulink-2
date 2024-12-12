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

#define DIGITAL_PIN_LISTENING_NUM 14 // Change 14 if you have a different number of pins.
#define ANALOG_PIN_LISTENING_NUM 6 // Change 6 if you have a different number of pins.

String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)
String rplyResult = "";

boolean pinListening[DIGITAL_PIN_LISTENING_NUM + ANALOG_PIN_LISTENING_NUM] = { false }; // Array used to know which pins on the Arduino must be listening.
int pinListenedValue[DIGITAL_PIN_LISTENING_NUM + ANALOG_PIN_LISTENING_NUM] = { -1 }; // Array used to know which value is read last time.

#define UNLIMITED_LENGTH ((size_t)-1)

struct CommandHandler {
    const char* command;
    bool (*handler)(const char* params, size_t length);
};


bool parseIntPair(const char* cParams, int& first, int& second, char separator = '/') {
  const char* separatorPos = strchr(cParams, separator);
  if (!separatorPos) return false;
  first = atoi(cParams);
  second = atoi(separatorPos + 1);
  return true;
}

bool setListeningState(int pinIndex, bool listening) {
  pinListening[pinIndex] = listening;
  pinListenedValue[pinIndex] = -1; // Reset the listened value to -1.
  pinMode(pinIndex, listening ? INPUT : OUTPUT);
  return true;
}

bool handleKprs(const String& params, size_t length) {
  // here you can write your own code. For instance the commented code change pin intensity if you press 'a' or 's'
  // take the command and change intensity on pin 11 this is needed just as example for this sketch
  
//  static int intensity = 0;
//  char commandChar = params.charAt(3);
//  if (commandChar == 'a') { // If press 'a' less intensity
//    intensity = max(0, intensity - 1);
//    analogWrite(11, intensity );
//    return true;
//   } else if (commandChar == 's') { // If press 's' more intensity
//    intensity = min(125, intensity + 1);
//    analogWrite(11, intensity );
//    return true;
//  }
  return false;
}

bool handlePpin(const char* cParams, size_t length) {
  int pin, value;
  if (!parseIntPair(cParams, pin, value)) return false;
  pinMode(pin, OUTPUT);
  analogWrite(pin, value);
  return true;
}

bool handlePpsw(const char* cParams, size_t length) {
  int pin, value;
  if (!parseIntPair(cParams, pin, value)) return false;
  pinMode(pin, OUTPUT);
  digitalWrite(pin, value == 1 ? HIGH : LOW);
  return true;
}

bool handleTone(const char* cParams, size_t length) {
  const char* separator1 = strchr(cParams, '/');
  if (!separator1) return false;
  const char* separator2 = strchr(separator1 + 1, '/');
  if (!separator2) return false;

  int pin = atoi(cParams);
  int frequency = atoi(separator1 + 1);
  int duration = atoi(separator2 + 1);

  if (duration == -1) {
    tone(pin, frequency);
  } else {
    tone(pin, frequency, duration);
  }
  return true;
}

bool handleNotn(const char* cParams, size_t length) {
  int pin = atoi(cParams);
  noTone(pin);
  return true;
}

bool handleSrld(const char* cParams, size_t length) {
  int pin = atoi(cParams);
  return setListeningState(pin, true);
}

bool handleSpld(const char* cParams, size_t length) {
  int pin = atoi(cParams);
  return setListeningState(pin, false);
}

bool handleSrla(const char* cParams, size_t length) {
  int pin = atoi(cParams);
  return setListeningState(DIGITAL_PIN_LISTENING_NUM + pin, true);
}

bool handleSpla(const char* cParams, size_t length) {
  int pin = atoi(cParams);
  return setListeningState(DIGITAL_PIN_LISTENING_NUM + pin, false);
}

bool handleCust(const char* cParams, size_t length) {
  String params = String(cParams);
  if (length != UNLIMITED_LENGTH) params = params.substring(0, length);
  int separator = params.indexOf('/');
  String customId = params.substring(0, separator);
  String value = params.substring(separator + 1);
  return handleCustomMessage(customId, value);
}

const CommandHandler commandHandlers[] = {
    {"kprs", handleKprs},
    {"ppin", handlePpin},
    {"ppsw", handlePpsw},
    {"tone", handleTone},
    {"notn", handleNotn},
    {"srld", handleSrld},
    {"spld", handleSpld},
    {"srla", handleSrla},
    {"spla", handleSpla},
    {"cust", handleCust}
};

void setup() {
  Serial.begin(115200);
  while (!Serial); // Wait until Serial is connected  

  // Turn off everything (not on RXTX)
  for (int i = 2; i < DIGITAL_PIN_LISTENING_NUM; i++) {
    pinMode(i, OUTPUT);
    digitalWrite(i, LOW);
  }

  sendRply(0, true); // Send Rply to signal ready state
}

void loop() {
  if (stringComplete) {
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
        int id = atoi(idPositionPtr + 4); // Skip "?id=" part
        sendRply(id, ok);
      }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  
  // Send listen messages
  for (int i = 0; i < (DIGITAL_PIN_LISTENING_NUM + ANALOG_PIN_LISTENING_NUM); i++) {
    bool isAnalog = i >= DIGITAL_PIN_LISTENING_NUM;
    int pinIndex = isAnalog ? i - DIGITAL_PIN_LISTENING_NUM : i;

    // Check if the pin is being listened to
    if (pinListening[i]) {
      int value = isAnalog ? highPrecisionAnalogRead(pinIndex) : digitalRead(pinIndex);

      // If the value has changed, update and send the new value
      if (value != pinListenedValue[i]) {
        pinListenedValue[i] = value;

        Serial.print("alp://");
        Serial.print(isAnalog ? "ared" : "dred");
        Serial.print("/");
        Serial.print(pinIndex);
        Serial.print("/");
        Serial.print(value);
        Serial.print('\n');
        Serial.flush();
      }
    }
  }
}

void sendRply(int id, bool ok) {
  Serial.print("alp://rply/");
  Serial.print(ok ? "ok" : "ko");
  Serial.print("?id=");
  Serial.print(id);
  if (rplyResult.length() > 0) {
    Serial.print("&");
    Serial.print(rplyResult);
    rplyResult = "";
  }        
  Serial.print('\n'); // End of Message
  Serial.flush();
}

bool handleCustomMessage(String customId, String value) {
    // here you can write your own code. 
  return false;
}

// Reads 4 times and computes the average value
int highPrecisionAnalogRead(int pin) {
    const int numReadings = 4;
    int sum = 0;
    for (int i = 0; i < numReadings; i++) {
        sum += analogRead(pin);
    }
    return sum / numReadings;
}


/*
 SerialEvent occurs whenever a new data comes in the
 hardware serial RX.  This routine is run between each
 time loop() runs, so using delay inside loop can delay
 response.  Multiple bytes of data may be available.
 This is general code you can reuse.
 */
void serialEvent() {
  while (Serial.available() && !stringComplete) {
    // get the new byte:
    char inChar = (char) Serial.read();
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
    } else {
      // add it to the inputString:
      inputString += inChar;
    }
  }
}
