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

#include <Servo.h> 

/**
 * This Sketch should work if your Arduino library supports more than two servo motors see here: http://www.arduino.cc/en/Reference/ServoAttach
 * Otherwise you can use the sketch version with Servo Software library
 */

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

Servo servoHand;
int   servoHandPin = 9;  // pwm pin used to control the servo

Servo servoRotor;
int   servoRotorPin = 10;  // pwm pin used to control the servo

Servo servoArm1;
int   servoArm1Pin = 11;  // pwm pin used to control the servo

Servo servoArm2;
int   servoArm2Pin = 5;  // pwm pin used to control the servo

void setup() {

  Serial.begin(115200);
  while(!Serial); // Wait until Serial not connected (because difference between Leonardo and Micro with UNO and others)

  servoHand.attach(servoHandPin);  // attaches the servo on pin servopin to the servo object 
  servoHand.write(170); // move hand to its default position (it depends on your calibration)
  
  servoRotor.attach(servoRotorPin);  // attaches the servo on pin servopin to the servo object 
  servoRotor.write(70); // move it to its default position (it depends on your calibration)

  servoArm1.attach(servoArm1Pin);  // attaches the servo on pin servopin to the servo object 
  servoArm1.write(90); // move it to its default position (it depends on your calibration)

  servoArm2.attach(servoArm2Pin);  // attaches the servo on pin servopin to the servo object 
  servoArm2.write(90); // move it to its default position (it depends on your calibration)

  Serial.print("alp://rply/"); // Ardulink here I'm ready!
  Serial.print("ok?id=0");
  Serial.print('\n'); // End of Message
  Serial.flush();

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

        int separatorIdPosition = inputString.indexOf('/', 11 );
        int separatorReplyPosition = inputString.indexOf('?', 11 );
        if(separatorReplyPosition == -1) {
        	separatorReplyPosition = inputString.length();
        }
        String customId = inputString.substring(11, separatorIdPosition);
        String value = inputString.substring(separatorIdPosition + 1, separatorReplyPosition);
                
        if(customId == "servoHand") {
           servoHand.write(value.toInt());
        } else if(customId == "servoRotor") {
          servoRotor.write(value.toInt());
        } else if(customId == "servoArm1") {
          servoArm1.write(value.toInt());
        } else if(customId == "servoArm2") {
          servoArm2.write(value.toInt());
        } else {
          msgRecognized = false; // this sketch doesn't know other messages in this case command is ko (not ok)
        }
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



