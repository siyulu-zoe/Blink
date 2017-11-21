int IR = 0;
int light_threshold = 400;
long start_time = 0;
long old_time = 0;
long new_time = 0;
long time_blinking = 0;
long intentional_threshold = 500;
long long_threshold = 3000;
boolean in_blink = false;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  start_time = millis();
  delay(100);
}

void loop() {
  IR = analogRead(A2);
  if (IR < light_threshold) {
    if (in_blink == false) {
      old_time = millis();
      in_blink = true;
    }
  }
  else {
    if (in_blink == true) {
      in_blink = false;
      new_time = millis();
      time_blinking = new_time - old_time;
      //Serial.print(time_blinking); 
      if (time_blinking > intentional_threshold) {
        if (time_blinking > long_threshold) {
          Serial.print("up");
          delay(1000);
        }
        else {
          Serial.print("down");
          delay(1000);
        }
      }
    }
  }
  Serial.println(IR);
  delay(50);
}
