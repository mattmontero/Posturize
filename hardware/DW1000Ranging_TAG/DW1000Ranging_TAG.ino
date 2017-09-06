
#include <SPI.h> 
#include "DW1000Ranging.h"
#include "DW1000Device.h" 


typedef union {
 float floatingPoint;
 byte binary[4];
 unsigned long ulong;
} binaryFloat;


void printFloat(float v){
  binaryFloat b;
  b.floatingPoint = v;  
  Serial.write(b.binary[0]);  
  Serial.write(b.binary[1]);  
  Serial.write(b.binary[2]);  
  Serial.write(b.binary[3]);  
}

void printLong(unsigned long v){
  binaryFloat b;
  b.ulong = v; 
  Serial.write(b.binary[0]);  
  Serial.write(b.binary[1]);  
  Serial.write(b.binary[2]);  
  Serial.write(b.binary[3]);  
}

void showDeviceInfo(){
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
  DW1000Ranging.initCommunication(9, 10); //Reset and CS pin  
  showDeviceInfo(); 
  //define the sketch as anchor. It will be great to dynamically change the type of module
  DW1000Ranging.attachNewRange(newRange);
  //we start the module as a tag
  DW1000Ranging.startAsTag("00:00:22:EA:82:60:3B:9C", DW1000.MODE_LONGDATA_RANGE_ACCURACY);
   
}

void loop() { 
  DW1000Ranging.loop(); 
}

float d1 = 0;
float d2 = 0;

void newRange(){ 
  float d = DW1000Ranging.getDistantDevice()->getRange();
  float p = DW1000Ranging.getDistantDevice()->getRXPower();
  unsigned int a = DW1000Ranging.getDistantDevice()->getShortAddress();

  /*int idx = (a >> 8) - 49;
  if (idx == 0) d1 = d;
  if (idx == 1) d2 = d;
  Serial.print(d1);
  Serial.print("  ");
  Serial.println(d2);*/
  
  Serial.write(0x44);
  Serial.write(0xaa); 
  printLong(millis());
  Serial.write(a >> 8);
  Serial.write(a & 0xFF);
  printFloat(d);
  printFloat(p);  
  Serial.write(0x44);
  Serial.write(0xaa);  
  
  /*Serial.print("from: "); Serial.print(DW1000Ranging.getDistantDevice()->getShortAddress(), HEX);  
  Serial.print("\t Range: "); Serial.print(DW1000Ranging.getDistantDevice()->getRange()); Serial.print(" m"); 
  Serial.print("\t RX power: "); Serial.print(DW1000Ranging.getDistantDevice()->getRXPower()); Serial.println(" dBm");
  */
  
  /*dist = 0.9 * dist + 0.1 * d;
  power = 0.9* power + 0.1 * p;
  Serial.print("distance: ");
  Serial.print(dist);
  Serial.print(" m");
  Serial.print("  power: ");
  Serial.print(power);
  Serial.println(" dBm");*/
}


