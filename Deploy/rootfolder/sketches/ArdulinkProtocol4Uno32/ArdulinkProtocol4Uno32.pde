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

// int intensity = 0;               // led intensity this is needed just as example for this sketch
String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)

#define digitalPinListeningNum 40 // Change 40 if you have a different number of pins.
#define analogPinListeningNum 25 // Change 25 if you have a different number of pins.
boolean digitalPinListening[digitalPinListeningNum]; // Array used to know which pins on the Arduino must be listening.
boolean analogPinListening[analogPinListeningNum]; // Array used to know which pins on the Arduino must be listening.
int digitalPinListenedValue[digitalPinListeningNum]; // Array used to know which value is read last time.
int analogPinListenedValue[analogPinListeningNum]; // Array used to know which value is read last time.

void setup() {
  // initialize serial: (this is general code you can reuse)
  Serial.begin(115200);
  while(Serial.available() <= 0); // Wait until Serial not connected
  
  //set to false all listen variable
  int index = 0;
  for (index = 0; index < digitalPinListeningNum; index++) {
    digitalPinListening[index] = false;
    digitalPinListenedValue[index] = -1;
  }
  for (index = 0; index < analogPinListeningNum; index++) {
    analogPinListening[index] = false;
    analogPinListenedValue[index] = -1;
  }

  // Turn off everything (not on RXTX)
  for (index = 2; index < digitalPinListeningNum; index++) {
    pinMode(index, OUTPUT);
    digitalWrite(index, LOW);
  }
  
  // Turn off LED this is needed just as example for this sketch
//  analogWrite(11, intensity);
  
  // Read from 4 this is needed just as example for this sketch
//  pinMode(4, INPUT);
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
      
      if(inputString.substring(6,10) == "kprs") { // KeyPressed
        // here you can write your own code. For instance the commented code change pin intensity if you press 'a' or 's'
        // take the command and change intensity on pin 11 this is needed just as example for this sketch
        //char commandChar = inputString.charAt(14);
        //if(commandChar == 'a' and intensity > 0) { // If press 'a' less intensity
        //  intensity--;
        //  analogWrite(11,intensity);
        //} else if(commandChar == 's' and intensity < 125) { // If press 's' more intensity
        //  intensity++;
        //  analogWrite(11,intensity);
        //}
      } else if(inputString.substring(6,10) == "ppin") { // Power Pin Intensity (this is general code you can reuse)
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String intens = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          analogWrite(pin.toInt(),intens.toInt());
      } else if(inputString.substring(6,10) == "ppsw") { // Power Pin Switch (this is general code you can reuse)
          int separatorPosition = inputString.indexOf('/', 11 );
          String pin = inputString.substring(11,separatorPosition);
          String power = inputString.substring(separatorPosition + 1);
          pinMode(pin.toInt(), OUTPUT);
          if(power.toInt() == 1) {
            digitalWrite(pin.toInt(), HIGH);
          } else if(power.toInt() == 0) {
            digitalWrite(pin.toInt(), LOW);
          }
      } else if(inputString.substring(6,10) == "srld") { // Start Listen Digital Pin (this is general code you can reuse)
          String pin = inputString.substring(11);
          digitalPinListening[pin.toInt()] = true;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
          pinMode(pin.toInt(), INPUT);
      } else if(inputString.substring(6,10) == "spld") { // Stop Listen Digital Pin (this is general code you can reuse)
          String pin = inputString.substring(11);
          digitalPinListening[pin.toInt()] = false;
          digitalPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else if(inputString.substring(6,10) == "srla") { // Start Listen Analog Pin (this is general code you can reuse)
          String pin = inputString.substring(11);
          analogPinListening[pin.toInt()] = true;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else if(inputString.substring(6,10) == "spla") { // Stop Listen Analog Pin (this is general code you can reuse)
          String pin = inputString.substring(11);
          analogPinListening[pin.toInt()] = false;
          analogPinListenedValue[pin.toInt()] = -1; // Ensure a message back when start listen happens.
      } else if(inputString.substring(6,10) == "cust") { // Custom Message
          
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
  
  // Send listen messages
  int index = 0;
  for (index = 0; index < digitalPinListeningNum; index++) {
    if(digitalPinListening[index] == true) {
      int value = digitalRead(index);
      if(value != digitalPinListenedValue[index]) {
        digitalPinListenedValue[index] = value;
        Serial.print("alp://dred/");
        Serial.print(index);
        Serial.print("/");
        Serial.print(value);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }
  }
  for (index = 0; index < analogPinListeningNum; index++) {
    if(analogPinListening[index] == true) {
      int value = highPrecisionAnalogRead(index);
      if(value != analogPinListenedValue[index]) {
        analogPinListenedValue[index] = value;
        Serial.print("alp://ared/");
        Serial.print(index);
        Serial.print("/");
        Serial.print(value);
        Serial.write(255); // End of Message
        Serial.flush();
      }
    }
  }
}

// Reads 4 times and computes the average value
int highPrecisionAnalogRead(int pin) {
  int value1 = analogRead(pin);
  int value2 = analogRead(pin);
  int value3 = analogRead(pin);
  int value4 = analogRead(pin);
  
  int retvalue = (value1 + value2 + value3 + value4) / 4;
}


