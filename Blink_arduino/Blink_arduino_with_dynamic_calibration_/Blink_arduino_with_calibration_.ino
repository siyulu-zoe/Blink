#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"

#if SOFTWARE_SERIAL_AVAILABLE
  #include <SoftwareSerial.h>
#endif


    #define FACTORYRESET_ENABLE         1
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"

Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

//Global Variables
int IR = 0;
int light_threshold =0;
long start_time = 0; //milli start of whole program
long old_time = 0; //milli start of blink
long new_time = 0; //milli end of blink
long time_blinking = 0; //duration of blink in milli
long intentional_threshold = 500; //milli half second "down"
long long_threshold = 2000; //milli "up"
long help_threshold=3000;
boolean in_blink = false; //it is blinking?

bool calibration= true;
int calibration_values[300];
int pin=A1;

bool dblink=false;
bool between_dblink=false;
long dblink_time1=0;
long dblink_time2=0;
int dblink_threshold=150;

void setup(void)
{
//  while (!Serial);  // required for Flora & Micro
  delay(500);
  Serial.begin(115200);
  Serial.println(F("Adafruit Bluefruit Command <-> Data Mode Example"));
  Serial.println(F("------------------------------------------------"));
  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));
  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );
  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      error(F("Couldn't factory reset"));
    }
  }
  /* Disable command echo from Bluefruit */
  ble.echo(false);
  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();
  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  
  //WILL STOP HERE UNTIL CONNECTED TO PHONE APP//
  
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();
  ble.verbose(false);  // debug info is a little annoying after this point!

  
  /* Wait for connection */
  while (! ble.isConnected()) {
      delay(500);
  }
  
  Serial.println(F("******************************"));
  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }
  // Set module to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);
  Serial.println(F("******************************"));
  start_time = millis();
  delay(100);
}
int calibrate(void){
    int time1=0;
    int time2=0;
    int thresh;
    
    time1=millis();
    for (int i=0; i <= 299; i++){
      calibration_values[i]= analogRead(pin);
      delay(10);
        }
     time2=millis();
     int counter=0;
     long sum=0;   
     for (int z=50; z<=250; z++){
         sum=sum+calibration_values[z];
         counter=counter+1;
        }
  
  
     thresh=sum/counter;
     if (thresh>200){
      thresh=thresh;
      
      }
     calibration=false;
     Serial.println( "Light Threshold:" );
     Serial.println(thresh);
     Serial.println( "Calibration Time:" );
     Serial.println(time2-time1);
     return thresh;
    
    }

//MAIN LOOP//

void loop(void)
{
  //Read IR sensor data
  if (calibration){
    light_threshold=calibrate();
    calibration= false;
    }

 
  IR = analogRead(pin);
  if (IR < light_threshold) {
    if (in_blink == false) { //if past the threshold for first instance start timer (old time) and say its in a blink
      old_time = millis(); //get start time stamps
      in_blink = true; //in bink = true
//      if(between_dblink==true){
//        dblink_time2=millis();
//        between_dblink=false;
////        Serial.println("Defining time2:");
////         Serial.print(dblink_time2);
        }
      
      }
      
    
  else {
    if (in_blink == true) { //if back from threshold and first time above, end timer (new time) and calculate difference in time
      in_blink = false;
      new_time = millis();
      time_blinking = new_time - old_time;
      //Serial.print(time_blinking); 
      if (time_blinking > intentional_threshold) { //if above the threshold do something
//        dblink_time1=0;
//        dblink_time2=0;
//        if (time_blinking> help_threshold){
//          ble.print("help");
//          }
        if (time_blinking > long_threshold ) { //if above long threshold then send long blink
          ble.print("up");
          //Serial.print("up"); //used to debug
          delay(750);
        }
        else { //if below long threshold then send short blink signal
          ble.print("down");
          //Serial.print("down");
          delay(750);
        }
      }
      else{
//        Serial.print(dblink_time2);
//        Serial.print("|||||");
//        Serial.print(dblink_time1);
   
//        if (0<(dblink_time2-dblink_time1)&&(dblink_time2-dblink_time1)<dblink_threshold){
//          dblink_time1=0;
//          dblink_time2=0;
//          ble.print("double");
//          Serial.println("double");
//          }
//        
//         between_dblink=true;
//         dblink_time1=millis();
//         Serial.println("Defining time1:");
//         Serial.print(dblink_time1);
        
    }
  }
  //Show current IR data
  Serial.print(IR);
  Serial.print(" ");
  Serial.println(light_threshold);
  delay(50);
  }
  
  
  
  //CODE BELOW THIS LINE NOT USED//
  // Check for user input
  char n, inputs[BUFSIZE+1];

  if (Serial.available())
  {
    n = Serial.readBytes(inputs, BUFSIZE);
    inputs[n] = 0;
    // Send characters to Bluefruit
    Serial.print("Sending: ");
    Serial.println(inputs);

    // Send input data to host via Bluefruit
    ble.print(inputs);
  }

  // Echo received data
  while ( ble.available() )
  {
    int c = ble.read();

    Serial.print((char)c);

    // Hex output too, helps w/debugging!
    Serial.print(" [0x");
    if (c <= 0xF) Serial.print(F("0"));
    Serial.print(c, HEX);
    Serial.print("] ");
  }
}

  
  

