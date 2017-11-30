
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
#define CON_SEC_SLOUCH_NUM 5
#define OUTLIERS 5
#define SAMPLE_SIZE 15

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
  //Serial.print("Device ID: "); Serial.println(msg);
  DW1000.getPrintableExtendedUniqueIdentifier(msg);
  //Serial.print("Unique ID: "); Serial.println(msg);
  DW1000.getPrintableNetworkIdAndShortAddress(msg);
  //Serial.print("Network ID & Device Address: "); Serial.println(msg);
  DW1000.getPrintableDeviceMode(msg);
  //Serial.print("Device mode: "); Serial.println(msg);
}
  static double currentSum = 0;
  static double currentVar = 0;
  static double currentDev = 0;
  static float calibratedDev = 0;
  static float calibratedValue = 0;
  static float calibratedVariance = 0;
  static boolean isCalibrated = false;
  static int calibrationTriggerCounter = 0;
  static int isCalibrating = false;
  static float threshold = 0.06;
  static float calibrationAry[CALIBRATION_SIZE];
  static float slouchAvg[CON_SEC_SLOUCH_NUM];
  static int calibrationCounter = 0;
  static int bluetoothCalibration = false;

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
  switch(inData){
  case '*':
    bluetoothCalibration = true;
    break;
  case 'c':
    Serial.print("c#");
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
        calibrationCounter = 0;
        calibratedVariance = 0;
        calibratedDev = 0;
        calibratedValue = 0;
        isCalibrating = true;
        //should we also send signal to phone to show its calibrating via hardware trigger?
      }
    }else if(bluetoothCalibration){//false will be changed to bluetooth signal for calibration
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
  if(isCalibrating){
    if(calibrationCounter == 0) {
      //Serial.print("#");Serial.println("CALIBRATION STARTED.......");Serial.println(calibratedValue);
      digitalWrite(RELAY_PIN, HIGH);
      delay(800);
      digitalWrite(RELAY_PIN, LOW);
    }
    calibrationAry[calibrationCounter%CALIBRATION_SIZE] = DW1000Ranging.getDistantDevice()->getRange();
    //Serial.println(calibrationAry[calibrationCounter%CALIBRATION_SIZE]);
    calibrationCounter++;
    
    if(calibrationCounter%CALIBRATION_SIZE==CALIBRATION_SIZE-1){
      //calc sample avg and sample variance
      currentSum = getSampleMean(calibrationAry);
      currentVar = getSampleVariance(calibrationAry, currentSum);//standard deviation
      if(calibrationCounter/CALIBRATION_SIZE != 0){
        calibratedDev += sqrt(currentVar)/sqrt(CALIBRATION_SIZE-OUTLIERS*2);
        calibratedVariance += currentVar;
        calibratedValue += currentSum;
      }

      //Serial.print("SAMPLE DEVIATION");Serial.println(sqrt(currentVar)/sqrt(CALIBRATION_SIZE-OUTLIERS*2), 6);//standard deviation
      //Serial.print("SAMPLE MEAN:");Serial.println(currentSum, 6);
      //Serial.print("SAMPLE VAR:");Serial.println(currentVar, 6);
      //Serial.print("CALIBRATED VALUE :");Serial.println(calibratedValue, 6);
      //Serial.println(calibrationCounter);
      if(calibrationCounter == CALIBRATION_SIZE*SAMPLE_SIZE-1){
        calibratedVariance /= SAMPLE_SIZE-1;
        calibratedValue /= SAMPLE_SIZE-1;
        calibratedDev /= SAMPLE_SIZE-1;
        isCalibrated = true;
        isCalibrating = false;
        calibrationCounter = 0;
  
        bluetoothCalibration = false;
        //Serial.print("#");Serial.println("CALIBRATED SAMPLING DEV.......");Serial.println(calibratedDev, 6);
        //Serial.print("#");Serial.println("CALIBRATED SAMPLING MEAN.......");Serial.println(calibratedValue, 6);
        //Serial.print("#");Serial.println("CALIBRATED SAMPLING VARIANCE.......");Serial.println(calibratedVariance, 6);
        Serial.print("*");Serial.print("#");
        digitalWrite(RELAY_PIN, HIGH);
        delay(800);
        digitalWrite(RELAY_PIN, LOW);
      }
    }
    
  }else if(isCalibrated){//not calibrating....means need to check reading...
    if(pollCounter < POLLING_SIZE){
        calibrationAry[pollCounter] = DW1000Ranging.getDistantDevice()->getRange();
        pollCounter++;
    }else{

        //get poll mean
        currentSum = getSampleMean(calibrationAry);
        currentDev = sqrt(getSampleVariance(calibrationAry, currentSum))/sqrt(sqrt(CALIBRATION_SIZE-OUTLIERS*2));
        //compare it mean to calibrated mean with the variance
        //Serial.print(calibratedValue - calibratedDev*3, 6);Serial.print("  ::  ");Serial.println(calibratedDev, 6);
        //Serial.print(currentSum, 6);Serial.print("  ::  ");Serial.println(currentDev, 6);//Serial.println("#");//Serial.println("#");//Serial.println("#");
        
        if((calibratedValue - calibratedDev*2 > currentSum)){
        //if((currentDev < 2.5*calibratedDev)&&(calibratedValue - calibratedDev*3 > currentSum)){
            if(lastPollSlouch){
              slouchAvg[conSecSlouch++] = currentSum;
            }else{
              conSecSlouch=0;
            }
            lastPollSlouch = true;
            //Serial.print(conSecSlouch);Serial.println("...");
            if(conSecSlouch == CON_SEC_SLOUCH_NUM){
              digitalWrite(RELAY_PIN, HIGH);
              delay(400);
              digitalWrite(RELAY_PIN, LOW);
              delay(400);
              digitalWrite(RELAY_PIN, HIGH);
              delay(400);
              digitalWrite(RELAY_PIN, LOW);
              conSecSlouch=0;
              currentSum = 0;
              for(int i = 0; i < CON_SEC_SLOUCH_NUM; i++)
                currentSum += slouchAvg[i];
              Serial.print((currentSum/CON_SEC_SLOUCH_NUM - calibratedValue)/calibratedValue*100, 3);Serial.print(",#");
            }
            //Serial.print("...SLOUCH DETECTED.... ");
            
        }else{
            lastPollSlouch = false;
            //Serial.print("...WITH IN RANGE...NOTHING TO WORRY... ");
            //Serial.println(currentSum);//Serial.println("#");
            digitalWrite(RELAY_PIN, LOW);
        }
        pollCounter = 0;
        currentSum = 0;
    }  
  }
}

float getSampleMean(float* calibrationAry){
  float currentSum = 0;
  for(int outlierCounter = 0; outlierCounter < OUTLIERS; outlierCounter++){

          int maxIndex = 0;
          int minIndex = 0;
          
          for(int a = 0 + outlierCounter; a < CALIBRATION_SIZE - outlierCounter; a++){
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
  
  return currentSum/(CALIBRATION_SIZE-OUTLIERS*2);
}

float getSampleVariance(float* calibrationAry, float sampleMean) {
  float currentVar = 0;
    for(int a = 0+OUTLIERS; a < CALIBRATION_SIZE-OUTLIERS; a++){
      //Serial.println(calibrationAry[a]);
      calibrationAry[a] -= sampleMean;
      currentVar += calibrationAry[a]*calibrationAry[a];  
    }
    currentVar /= (CALIBRATION_SIZE-2*OUTLIERS);
    return currentVar;
}


