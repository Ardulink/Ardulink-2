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

#include <AFMotor.h>

String inputString = "";         // a string to hold incoming data (this is general code you can reuse)
boolean stringComplete = false;  // whether the string is complete (this is general code you can reuse)

AF_DCMotor motorLeft(3);
AF_DCMotor motorRight(4);

void setup() {
  // initialize serial:
  Serial.begin(9600);
  while(!Serial); // Wait until Serial not connected (because difference between Leonardo and Micro with UNO and others)
  
motorLeft.setSpeed(200);
motorRight.setSpeed(200);
 
motorLeft.run(RELEASE);
motorRight.run(RELEASE);  
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
      } else if(inputString.substring(6,10) == "cust") { // Custom Message
        int startPosition = inputString.indexOf('(', 11 );
        int endPosition = inputString.indexOf(']', 11 );
        String customId = inputString.substring(11, startPosition);
        String values = inputString.substring(startPosition + 1, endPosition);
        if(customId == "joy") {
          drive(values);
        } else {
          msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
        }
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
		Serial.print('\n'); // End of Message
        Serial.flush();
      }
    }
    
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
}


void drive(String values) {
  String leftDirection = values.substring(0, 1);
  int leftPowerEndPosition = values.indexOf(')');
  String leftPower = values.substring(1, leftPowerEndPosition);
  String rightDirection = values.substring(leftPowerEndPosition + 2, leftPowerEndPosition + 3);
  String rightPower = values.substring(leftPowerEndPosition + 3, values.length());
//  Serial.println(leftDirection);
//  Serial.println(leftPower);
//  Serial.println(rightDirection);
//  Serial.println(rightPower);
//  Serial.println("--");
//  Serial.flush();
  
  if(leftDirection == "F") {
    motorLeft.run(FORWARD);
  } else {
    motorLeft.run(BACKWARD);
  }
  if(rightDirection == "F") {
    motorRight.run(FORWARD);
  } else {
    motorRight.run(BACKWARD);
  }

  motorLeft.setSpeed(leftPower.toInt());  
  motorRight.setSpeed(rightPower.toInt());  
}


