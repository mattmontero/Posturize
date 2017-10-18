
#include <SPI.h>
#include "DW1000Ranging.h"
#include "DW1000Device.h"

#include <SoftwareSerial.h>

#define RXPIN 0 //bluetooth pins
#define TXPIN 1 //bluetooth pins
#define RELAY_PIN 8 //relay signal pin; high = buzz 
#define CALIBRATE 7 //calibrationTrigger pin(hardware)
#define POLLING_SIZE 25
#define CALIBRATION_SIZE 25
#define CON_SEC_SLOUCH_NUM 4

#define BLUETOOTH_CALIBRATION 'C'
#define BLUETOOTH_IDLE 'I'

//create instance of SoftwareSerial
SoftwareSerial bluetooth(RXPIN, TXPIN);


//define button pin
const short buttonPin = 12;

void showDeviceInfo() {
  // DEBUG chip info and registers pretty printed
  char msg[256];
  DW1000.getPrintableDeviceIdentifier(msg);
  Serial.print("Device ID: "); Serial.println(msg);
  DW1000.getPrintableExtendedUniqueIdentifier(msg);
  Serial.print("Unique ID: "); Serial.println(msg);
  DW1000.getPrintableNetworkIdAndShortAddress(msg);
  Serial.print("Network ID & Device Address: "); Serial.println(msg);
  DW1000.getPrintableDeviceMode(msg);
  Serial.print("Device mode: "); Serial.println(msg);
}


  static boolean isCalibrated = false;
  static int calibrationTriggerCounter = 0;
  static int isCalibrating = false;
  static float threshold = 0.05;
  static float calibratedValue = 0;
  static float calibrationAry[25];
  static int calibrationCounter = 0;
  static int bluetoothCalibration = false;
  static double currentSum = 0;
  static int pollCounter = 0;
  static boolean lastPollSlouch = false;
  static int conSecSlouch = 0;
void setup() {
  Serial.begin(9600);
  delay(1000);
  //init the configuration

  pinMode(RELAY_PIN, OUTPUT);
  pinMode(CALIBRATE, INPUT);  

  
  // Define pin #12 as input and activate the internal pull-up resistor
  pinMode(buttonPin, INPUT_PULLUP);
  
  pinMode(RXPIN, INPUT);
  pinMode(TXPIN, OUTPUT);

  //Setupt Bluetooth serial connection to android
  bluetooth.begin(9600); //Start software Serial
  

  DW1000Ranging.initCommunication(9, 10); //Reset and CS pin
  showDeviceInfo();
  //define the sketch as anchor. It will be great to dynamically change the type of module
  DW1000Ranging.attachNewRange(newRange);
  //we start the module as an anchor
  //DW1000Ranging.startAsAnchor("01:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  //DW1000Ranging.startAsAnchor("02:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  DW1000Ranging.startAsAnchor("03:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
}

void checkForBTData(char inData){
  Serial.println("...checking for BT com channel for data....");
  switch(inData){
  case '*':
    bluetoothCalibration = true;
    break;
  default:
    break;
  }
}

void loop() {
  DW1000Ranging.loop();

  //Trigger to start calibration...
  if(!isCalibrating){
    if(digitalRead(CALIBRATE) == HIGH){
      calibrationTriggerCounter++;

      if(calibrationTriggerCounter > 3000){
        isCalibrating = true;
        calibratedValue = 0;
        //should we also send signal to phone to show its calibrating via hardware trigger?
        Serial.println("CALIBRATION BUTTON PRESSED");
      }
    }else if(bluetoothCalibration){//false will be changed to bluetooth signal for calibration
      //sending signal to phone
      if(bluetooth.available()){
        Serial.print("*");
        Serial.print("#");
      }

      isCalibrating = true;
      calibratedValue = 0;
    }
  }else{
    calibrationTriggerCounter = 0;
  }

  if(bluetooth.available()> 0){
    checkForBTData(bluetooth.read());
  }
}

void newRange() {
//  Serial.print("from: "); Serial.print(DW1000Ranging.getDistantDevice()->getShortAddress(), HEX);
//  Serial.print("\t Range: "); Serial.print(DW1000Ranging.getDistantDevice()->getRange()); Serial.print(" m");
//  Serial.print("\t RX power: "); Serial.print(DW1000Ranging.getDistantDevice()->getRXPower()); Serial.println(" dBm"); 
  if(isCalibrating && calibrationCounter%25 != 24 && calibrationCounter < 250){
    if(calibrationCounter == 0) {
      Serial.print("#");Serial.println("CALIBRATION STARTED.......");Serial.println(calibratedValue);
      digitalWrite(RELAY_PIN, HIGH);
      delay(800);
      digitalWrite(RELAY_PIN, LOW);
    }

    calibrationAry[calibrationCounter%25] = DW1000Ranging.getDistantDevice()->getRange();
    calibrationCounter++;
  }else if(isCalibrating && (calibrationCounter%25==24 || calibrationCounter >= 250)){
    
    calibrationAry[calibrationCounter%25] = DW1000Ranging.getDistantDevice()->getRange();
    calibrationCounter++;
    currentSum = 0;
    //sort top 3 and bottom 3 element of array to remove outliers
    for(int outlierCounter = 0; outlierCounter < 3; outlierCounter++){
      int maxIndex = 0;
      int minIndex = 0;
      double tempVal = 0;
        for(int a = 0 + outlierCounter; a < CALIBRATION_SIZE - outlierCounter; a++){
          //iterate the non elimited readings to find new max and min
          if(calibrationAry[a] > calibrationAry[maxIndex]){
            maxIndex = a;
          }
  
          if(calibrationAry[a] < calibrationAry[minIndex]){
            minIndex = a;
          }
          
          if(outlierCounter == 0){//takes the running sum of all 25 polled values
            currentSum += calibrationAry[a];
          }
        }

          currentSum -= calibrationAry[maxIndex];
          currentSum -= calibrationAry[minIndex];

        //swapping the max and min to the edge of array so it wont be used next iteration.
        tempVal = calibrationAry[maxIndex];
        calibrationAry[maxIndex] = calibrationAry[CALIBRATION_SIZE - outlierCounter - 1];
        calibrationAry[CALIBRATION_SIZE - outlierCounter - 1] = tempVal;

        tempVal = calibrationAry[minIndex];
        calibrationAry[minIndex] = calibrationAry[0 + outlierCounter];
        calibrationAry[0 + outlierCounter] = tempVal;
    }

    currentSum /= 19;
    calibratedValue += currentSum;
    if(calibrationCounter >= 249){
      calibratedValue /= 10;
      isCalibrated = true;
      isCalibrating = false;
      calibrationCounter = 0;

      bluetoothCalibration = false;
      Serial.print("#");Serial.println("CALIBRATION COMPLETED.......");Serial.println(calibratedValue);Serial.println("C");Serial.print("#");
      digitalWrite(RELAY_PIN, HIGH);
      delay(800);
      digitalWrite(RELAY_PIN, LOW);
    }

    
  }else if(isCalibrated){//not calibrating....means need to check reading...
    
    if(pollCounter < POLLING_SIZE){
        calibrationAry[pollCounter] = DW1000Ranging.getDistantDevice()->getRange();
        pollCounter++;
    }else{

        currentSum = 0;
        //iterate the array to find total, and find current iteration outlier
        for(int outlierCounter = 0; outlierCounter < 3; outlierCounter++){

          int maxIndex = 0;
          int minIndex = 0;
          
          for(int a = 0 + outlierCounter; a < POLLING_SIZE - outlierCounter; a++){
            if(calibrationAry[a] > calibrationAry[maxIndex]){
              maxIndex = a;
            }
    
            if(calibrationAry[a] < calibrationAry[minIndex]){
              minIndex = a;
            }
            
            if(outlierCounter == 0){//takes the running sum of all polled values
              currentSum += calibrationAry[a];
            }
          }

          currentSum -= calibrationAry[maxIndex];
          currentSum -= calibrationAry[minIndex];
          
          //swapping the max and min to the edge of array so it wont be used next iteration.
          double tempVal = calibrationAry[maxIndex];
          calibrationAry[maxIndex] = calibrationAry[POLLING_SIZE - outlierCounter - 1];
          calibrationAry[POLLING_SIZE - outlierCounter - 1] = tempVal;
  
          tempVal = calibrationAry[minIndex];
          calibrationAry[minIndex] = calibrationAry[0 + outlierCounter];
          calibrationAry[0 + outlierCounter] = tempVal;
        }
        currentSum /= 19;
        if(calibratedValue + threshold < currentSum ||
            calibratedValue - threshold > currentSum ){
            if(lastPollSlouch){
              conSecSlouch++;
            }else{
              conSecSlouch=0;
            }
            lastPollSlouch = true;
            
            if(conSecSlouch == CON_SEC_SLOUCH_NUM){
              digitalWrite(RELAY_PIN, HIGH);
              delay(400);
              digitalWrite(RELAY_PIN, LOW);
              delay(400);
              digitalWrite(RELAY_PIN, HIGH);
              delay(400);
              digitalWrite(RELAY_PIN, LOW);
              conSecSlouch=0;
            }
            Serial.print("...SLOUCH DETECTED.... ");Serial.print(currentSum);Serial.println("#");
        }else{
            lastPollSlouch = false;
            Serial.print("...WITH IN RANGE...NOTHING TO WORRY... ");Serial.print(currentSum);Serial.println("#");
            digitalWrite(RELAY_PIN, LOW);

        }
        
        pollCounter = 0;
        currentSum = 0;
    }
  
  
  
  
  
  }
}
