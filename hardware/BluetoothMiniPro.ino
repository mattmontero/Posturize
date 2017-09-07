#include <SoftwareSerial.h>

#define rxPin 0 //define rx data pin
#define txPin 1 //define tx data pin

// Declare the pins for the Button and the LED
const short buttonPin = 12;
const short RED_LED = 13;
const short GREEN_LED = 6;
const short WRITE_LED = 4;
short counter = 0;
char commandChar;

//create instance of SoftwareSerial
SoftwareSerial bluetooth(rxPin, txPin);


void setup() {
  // Define pin #12 as input and activate the internal pull-up resistor
  pinMode(buttonPin, INPUT_PULLUP);
  // Define pin #13 as output, for the LED
  pinMode(RED_LED, OUTPUT);
  pinMode(GREEN_LED, OUTPUT);

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
  if (buttonValue == LOW) { //Button Pressed

    // If button pushed, turn LED on
    digitalWrite(GREEN_LED, HIGH);
    digitalWrite(RED_LED, LOW);

    if (bluetooth.available()) {
      Serial.print("Hardware Pressed");
      Serial.print("#");
      //bluetooth.print("Hardware Pressed#");
      delay(3000);
    }

  } else { //Button not pressed.
    digitalWrite(GREEN_LED, LOW);
    digitalWrite(RED_LED, HIGH);
    //Turn off LED
    //byte index = 0;



    /*
      if (bluetooth.available() > 0) {
      char inChar;
      if (index < 63) {
        inChar = bluetooth.read(); //Read a char
        inData[index] = inChar; //Store char in inData string
        index++;
        if (index == 63) {
          inData[index] = '#'; //Null terminate the string
        }
      }
      if (index < 63) {
        inData[index] = '#';
      }
      for (int i = 0; i < 64; i++){
        Serial.print(inData[i]);
      }
      Serial.print("#");
      delay(2000);

      for (byte i = 0; i < 2; i++) {
        switch (inChar) {
          case '*':
            //Serial.print("You sent a star!\n#");
            break;
          case 'm':
            // Serial.print("You sent an m!\n#");
            break;
          default:
            Serial.print("You sent a ... ");
            Serial.print(inData[i]);
            Serial.print("\n#");
            delay(1000);

            break;
        }
      }
      }*/
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
