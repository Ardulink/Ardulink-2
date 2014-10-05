#define INDEX 14

int lastValue = -1;

void setup() {
  Serial.begin(115200);
  while(Serial.available() <= 0); // Wait until Serial not connected
}

void loop() {
  int value = highPrecisionAnalogRead(INDEX);
  if(lastValue != value) {
    lastValue = value;
    Serial.print("alp://ared/");
    Serial.print(INDEX);
    Serial.print("/");
    Serial.print(value);
    Serial.write(255); // End of Message
    Serial.flush();
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
