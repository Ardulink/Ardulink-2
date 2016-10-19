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

void resetUniqueID() {

  char buffer[UNIQUE_ID_LENGTH + 1];
  for(int i = 0; i < UNIQUE_ID_LENGTH + 1; i++) {
    buffer[i] = 0;
  }
  EEPROM.put( UNIQUE_ID_EEPROM_ADDRESS, buffer );
}

void setup(){
  resetUniqueID();
}

void loop(){
}

