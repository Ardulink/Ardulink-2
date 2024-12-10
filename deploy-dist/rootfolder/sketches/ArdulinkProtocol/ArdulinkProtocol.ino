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

String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)
String rplyResult = "";

#define digitalPinListeningNum 14 // Change 14 if you have a different number of pins.
#define analogPinListeningNum 6 // Change 6 if you have a different number of pins.
boolean digitalPinListening[digitalPinListeningNum] = { false }; // Array used to know which pins on the Arduino must be listening.
boolean analogPinListening[analogPinListeningNum] = { false }; // Array used to know which pins on the Arduino must be listening.
int digitalPinListenedValue[digitalPinListeningNum] = { -1 }; // Array used to know which value is read last time.
int analogPinListenedValue[analogPinListeningNum] = { -1 }; // Array used to know which value is read last time.

void setup() {
  Serial.begin(115200);
  while (!Serial); // Wait until Serial is connected  
  sendRply("0", true); // Send Rply to signal ready state

  // Turn off everything (not on RXTX)
  for (int i = 2; i < digitalPinListeningNum; i++) {
    pinMode(i, OUTPUT);
    digitalWrite(i, LOW);
  }
}

void loop() {
  // when a newline arrives:
  if (stringComplete) {
    
    if (inputString.startsWith("alp://")) { // OK is a message I know (this is general code you can reuse)
    
      boolean msgRecognized = true;
      
      int idPosition = inputString.indexOf("?id=");
      if (inputString.substring(6,10) == "kprs") { // KeyPressed
          String message = inputString.substring(11, idPosition < 0 ? inputString.length() : idPosition);
          return handleKeyPressed(message);
      } else if (inputString.substring(6,10) == "ppin") { // Power Pin Intensity (this is general code you can reuse)
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String intens = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          analogWrite(pin.toInt(),intens.toInt());
      } else if (inputString.substring(6,10) == "ppsw") { // Power Pin Switch (this is general code you can reuse)
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String power = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          digitalWrite(pin.toInt(), power.toInt() == 1 ? HIGH : LOW);
      } else if (inputString.substring(6,10) == "tone") { // tone request (this is general code you can reuse)
          int firstSlashPosition = inputString.indexOf('/', 11 );
          int secondSlashPosition = inputString.indexOf('/', firstSlashPosition + 1 );
          int pin = inputString.substring(11,firstSlashPosition).toInt();
          int frequency = inputString.substring(firstSlashPosition + 1, secondSlashPosition).toInt();
          int duration = inputString.substring(secondSlashPosition + 1).toInt();
          if (duration == -1) {
          	tone(pin, frequency);
          } else {
          	tone(pin, frequency, duration);
          }
      } else if (inputString.substring(6,10) == "notn") { // no tone request (this is general code you can reuse)
          int firstSlashPosition = inputString.indexOf('/', 11 );
          int pin = inputString.substring(11,firstSlashPosition).toInt();
          noTone(pin);
      } else if (inputString.substring(6,10) == "srld") { // Start Listen Digital Pin (this is general code you can reuse)
          String pin = inputString.substring(11, idPosition < 0 ? inputString.length() : idPosition);
          digitalPinListening[pin.toInt()] = true;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), INPUT);
      } else if (inputString.substring(6,10) == "spld") { // Stop Listen Digital Pin (this is general code you can reuse)
          String pin = inputString.substring(11, idPosition < 0 ? inputString.length() : idPosition);
          digitalPinListening[pin.toInt()] = false;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), OUTPUT);
      } else if (inputString.substring(6,10) == "srla") { // Start Listen Analog Pin (this is general code you can reuse)
          String pin = inputString.substring(11, idPosition < 0 ? inputString.length() : idPosition);
          analogPinListening[pin.toInt()] = true;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), INPUT);
      } else if (inputString.substring(6,10) == "spla") { // Stop Listen Analog Pin (this is general code you can reuse)
          String pin = inputString.substring(11, idPosition < 0 ? inputString.length() : idPosition);
          analogPinListening[pin.toInt()] = false;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), OUTPUT);
      } else if (inputString.substring(6,10) == "cust") { // Custom Message
          int firstSlashPosition = inputString.indexOf('/', 11 );
          String customId = inputString.substring(11, firstSlashPosition);
          String value = inputString.substring(firstSlashPosition + 1, idPosition < 0 ? inputString.length() : idPosition);
          msgRecognized = handleCustomMessage(customId, value);
      } else {
          msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
      }
      
      // Prepare reply message if caller supply a message id (this is general code you can reuse)
      if (idPosition != -1) {
        String id = inputString.substring(idPosition + 4);
        sendRply(id, msgRecognized);
      }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  
  // Send listen messages
  for (int i = 0; i < digitalPinListeningNum; i++) {
    if (digitalPinListening[i]) {
      int value = digitalRead(i);
      if (value != digitalPinListenedValue[i]) {
        digitalPinListenedValue[i] = value;
        sendPinReading("dred", i, value);
      }
    }
  }
  for (int i = 0; i < analogPinListeningNum; i++) {
    if (analogPinListening[i]) {
      int value = highPrecisionAnalogRead(i);
      if (value != analogPinListenedValue[i]) {
        analogPinListenedValue[i] = value;
        sendPinReading("ared", i, value);
      }
    }
  }
}

void sendPinReading(const char* type, int pin, int value) {
    Serial.print("alp://");
    Serial.print(type);
    Serial.print("/");
    Serial.print(pin);
    Serial.print("/");
    Serial.print(value);
    Serial.print('\n'); // End of Message
    Serial.flush();
}

void sendRply(String id, boolean ok) {
  // print the reply
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

bool handleKeyPressed(String message) {
  // here you can write your own code. For instance the commented code change pin intensity if you press 'a' or 's'
  // take the command and change intensity on pin 11 this is needed just as example for this sketch
  
  // static int intensity = 0;
  // char commandChar = message.charAt(3);
  // if (commandChar == 'a') { // If press 'a' less intensity
  //   analogWrite(11, max(0, --intensity));
  //   return true;
  // } else if (commandChar == 's') { // If press 's' more intensity
  //   analogWrite(11, min(125, ++intensity));
  //   return true;
  // }
  return false;
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
