#define INDEX 14

int lastValue = -1;

void setup() {
  Serial.begin(115200);

  // pingprobe
  Serial.print("alp://rply/");
  Serial.print("ok?id=0");
  Serial.print('\n'); // End of Message
  Serial.flush();

}

void loop() {
  int value = highPrecisionAnalogRead(INDEX);
  if(lastValue != value) {
    lastValue = value;
    Serial.print("alp://ared/");
    Serial.print(INDEX);
    Serial.print("/");
    Serial.print(value);
    Serial.print('\n'); // End of Message
    Serial.flush();
  }
}

// Reads 4 times and computes the average value
int highPrecisionAnalogRead(int pin) {
  int value1 = analogRead(pin);
  int value2 = analogRead(pin);
  int value3 = analogRead(pin);
  int value4 = analogRead(pin);
  
  return (value1 + value2 + value3 + value4) / 4;
  return retvalue;
}
