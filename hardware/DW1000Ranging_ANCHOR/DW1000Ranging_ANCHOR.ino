
#include <SPI.h>
#include "DW1000Ranging.h"
#include "DW1000Device.h"

#include <SoftwareSerial.h>

#define RXPIN 0 //bluetooth pins
#define RXPIN 1 //bluetooth pins
#define BUZZER 8 // 
#define CALIBRATE 7 //calibrationTrigger pin(hardware)
#define POLLING_SIZE 10
#define CALIBRATION_SIZE 25

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


void setup() {
  Serial.begin(115200);
  delay(1000);
  //init the configuration

  pinMode(BUZZER, OUTPUT);
  pinMode(CALIBRATE, INPUT);  

  DW1000Ranging.initCommunication(9, 10); //Reset and CS pin
  showDeviceInfo();
  //define the sketch as anchor. It will be great to dynamically change the type of module
  DW1000Ranging.attachNewRange(newRange);
  //we start the module as an anchor
  //DW1000Ranging.startAsAnchor("01:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  //DW1000Ranging.startAsAnchor("02:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
  DW1000Ranging.startAsAnchor("03:00:5B:D5:A9:9A:E2:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
}

static boolean isCalibrated = false;
static int calibrationTriggerCounter = 0;
static int isCalibrating = false;
static float threshold = 0.05;
static float calibratedValue = 0;
static float calibrationAry[25];
static int calibrationCounter = 0;
static int bluetoothCalibration = false;

void checkForBTData(){
  Serial.println("...checking for BT com channel for data....");
  //checks the bt serial com channel for data....
  //switch(incData){
  //case btCalibration:
      //bluetoothCalibration = true;
  //case anotherCase: 
      //doSomething else
  //default:
      //doNothing
}

void loop() {
  DW1000Ranging.loop();

  //Trigger to start calibration...
  if(!isCalibrating){
    if(digitalRead(CALIBRATE) == HIGH){
      calibrationTriggerCounter++;
      if(calibrationTriggerCounter > 1500){
        isCalibrating = true;
        calibratedValue = 0;
        //should we also send signal to phone to show its calibrating via hardware trigger?
      }
    }else if(bluetoothCalibration){//false will be changed to bluetooth signal for calibration
      isCalibrating = true;
      calibratedValue = 0;
    }
  }else{
    calibrationTriggerCounter = 0;
  }

  //checkForBTData();
}


static double currentSum = 0;
static int pollCounter = 0;

void newRange() {
//  Serial.print("from: "); Serial.print(DW1000Ranging.getDistantDevice()->getShortAddress(), HEX);
//  Serial.print("\t Range: "); Serial.print(DW1000Ranging.getDistantDevice()->getRange()); Serial.print(" m");
//  Serial.print("\t RX power: "); Serial.print(DW1000Ranging.getDistantDevice()->getRXPower()); Serial.println(" dBm");

//  if(isCalibrating && calibrationCounter < 250){ //sampling 300 readings to get good variations of data
//    Serial.print(DW1000Ranging.getDistantDevice()->getRange(), 4);Serial.print(" counter is @ "); Serial.println(calibrationCounter);
//    if(calibrationCounter < 25)
//      calibrationAry[calibrationCounter%25] = DW1000Ranging.getDistantDevice()->getRange();
//    else
//      calibrationAry[calibrationCounter%25] += DW1000Ranging.getDistantDevice()->getRange();
//    calibrationCounter++;
//  }else if(isCalibrating && calibrationCounter >= 250){
//    //send signal to phone to trigger end of calibration
//    isCalibrating = false;
//
//    float totalSum = 0;
//    for(int a = 0; a < 25; a++){
//      totalSum += calibrationAry[a]/10;
//    }
//    calibratedValue = totalSum/25;
//    isCalibrated = true;
//    isCalibrating = false;
//    calibrationCounter = 0;
//    Serial.print("calibratedValue is : ");
//    Serial.println(calibratedValue, 4);
//    //DO STATS TO GET THRESHOLD AND CRALIBRATED VALUE
//    //each index contains 
  if(isCalibrating && calibrationCounter%25 != 24 && calibrationCounter < 250){
    Serial.print(DW1000Ranging.getDistantDevice()->getRange(), 4);Serial.print(" counter is @ "); Serial.println(calibrationCounter);
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
         
          Serial.println(currentSum);
          currentSum -= calibrationAry[maxIndex];
          Serial.println(currentSum);
          currentSum -= calibrationAry[minIndex];
          Serial.println("CURRENT SUM IN LOOP...");Serial.println(currentSum);

        //swapping the max and min to the edge of array so it wont be used next iteration.
        tempVal = calibrationAry[maxIndex];
        calibrationAry[maxIndex] = calibrationAry[CALIBRATION_SIZE - outlierCounter - 1];
        calibrationAry[CALIBRATION_SIZE - outlierCounter - 1] = tempVal;

        tempVal = calibrationAry[minIndex];
        calibrationAry[minIndex] = calibrationAry[0 + outlierCounter];
        calibrationAry[0 + outlierCounter] = tempVal;
    }


        for(int a = 0; a < 25; a++){
          Serial.println(calibrationAry[a]); 
        }
    
    currentSum /= 19;
    calibratedValue += currentSum;
    if(calibrationCounter >= 249){
      calibratedValue /= 10;
      isCalibrated = true;
      isCalibrating = false;
      calibrationCounter = 0;
    }    
    Serial.print("THE Calibration AVERAGE IS...");Serial.println(calibratedValue);
    
  }else if(isCalibrated){//not calibrating....means need to check reading...
    
    if(pollCounter < POLLING_SIZE){
        calibrationAry[pollCounter] = DW1000Ranging.getDistantDevice()->getRange();
        pollCounter++;
    }else{
        
        //iterate the array to find total, and find current iteration outlier
        for(int outlierCounter = 0; outlierCounter < 2; outlierCounter++){
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

          Serial.println(currentSum);
          
          currentSum -= calibrationAry[maxIndex];
          
          Serial.println(currentSum);
          currentSum -= calibrationAry[minIndex];
          Serial.println("CURRENT SUM IN LOOP...");Serial.println(currentSum);

          //swapping the max and min to the edge of array so it wont be used next iteration.
          double tempVal = calibrationAry[maxIndex];
          calibrationAry[maxIndex] = calibrationAry[POLLING_SIZE - outlierCounter - 1];
          calibrationAry[POLLING_SIZE - outlierCounter - 1] = tempVal;
  
          tempVal = calibrationAry[minIndex];
          calibrationAry[minIndex] = calibrationAry[0 + outlierCounter];
          calibrationAry[0 + outlierCounter] = tempVal;
        }

        for(int a = 0; a < 10; a++){
          Serial.println(calibrationAry[a]); 
        }
    
        currentSum /= 6;
        
        Serial.print("...CURRENT SUM....");
        Serial.println(currentSum);


        if(calibratedValue + threshold < currentSum ||
            calibratedValue - threshold > currentSum ){
              Serial.print("upper bound limit");
              Serial.print(calibratedValue + threshold);
              Serial.print("   distance from dwm:");
              Serial.println(currentSum);
              
              Serial.print("lower bound limit");
              Serial.print(calibratedValue - threshold);
              Serial.print("   distance from dwm:");
              Serial.println(currentSum);
              
              Serial.println("...SLOUCH DETECTED.... ");
  
              //NEED TO SEND DATA TO PHONE
              
        }else{
            Serial.println("...WITH IN RANGE...NOTHING TO WORRY...");
        }
        
        pollCounter = 0;
        currentSum = 0;
    }
  
  
  
  
  
  }
}
/* todos....
 * test and debug STAGE 2 calibration and polling/reading distance. Code theory crafted
 * wired the switch for buzzer?
 * integrate bluetooth calibration to the hardwares. debug and test.
 */
