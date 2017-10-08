#include <SoftwareSerial.h>

#define rxPin 0 //define rx data pin
#define txPin 1 //define tx data pin

//define button pin
const short buttonPin = 12;

//create instance of SoftwareSerial
SoftwareSerial bluetooth(rxPin, txPin);


void setup() {
  // Define pin #12 as input and activate the internal pull-up resistor
  pinMode(buttonPin, INPUT_PULLUP);

  pinMode(rxPin, INPUT);
  pinMode(txPin, OUTPUT);

  //Setup usb serial connection to computer
  Serial.begin(9600);    //Start hardware Serial

  //Setupt Bluetooth serial connection to android
  bluetooth.begin(9600); //Start software Serial
}

void loop() {
  // Read the value of the input. It can either be 1 or 0
  int buttonValue = digitalRead(buttonPin);
  if (buttonValue == LOW && bluetooth.available()) { //Button Pressed
      Serial.print("Hardware Pressed");
      Serial.print("#");
      delay(3000);
  } 
    delay(1000);
    typedef union {
      float floatingPoint;
      byte bytes[4];
    } floatValue;

    floatValue randFloat;
    randFloat.floatingPoint = (float)rand() / (float)(RAND_MAX / 100);
    if (bluetooth.available()) {
      //Serial.write(randFloat.bytes, 4);
      Serial.print(randFloat.floatingPoint);
      Serial.print("#");
    }
}
