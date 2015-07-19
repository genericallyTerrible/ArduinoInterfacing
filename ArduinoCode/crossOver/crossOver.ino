/*
  EEPROM block setup:
  0,        1,                                           ? - 511;
  endState, period, **Space for more variable storage**, password(stored backwards dynamically);
*/
#include <EEPROM.h>
#define LED 13
#define MAX_PROM 511

//commands
const char* CMD_LIST[]     = {"Update", "Blink", "Strobe", "Lock"};
const char* UPDATE_LIST[]  = {"Period", "Password"};
const int CMD_LIST_SIZE    = 5; //Maintain manually. Value is one grerater than the number of items in the array. sizeOf() does not work on char* []
const int UPDATE_LIST_SIZE = 3; //Maintain manually. Value is one grerater than the number of items in the array. sizeOf() does not work on char* []

//directions for fadeLoop()
const boolean IN_OUT = true;
const boolean OUT_IN = !IN_OUT;
//Global variable initializations
String password = "";
int passwordAttempts = 5;

int endState = 0;
int ledValue = 0;
int period = 0;
int temp = 0;
long startTime = 0;
long sysTime = 0;
String input = "";
String strIn = "\0";
boolean foundCmd = false;


void setup() {

  endState = EEPROM.read(0);
  period = (10 * EEPROM.read(1));

  char input = '\0';
  int address = MAX_PROM;

  input = EEPROM.read(address--);
  while (input != NULL){
    password += input;
    input = EEPROM.read(address--);
  }

  pinMode(LED, OUTPUT);
  analogWrite(LED, 255);
  Serial.begin(9600);
  Serial.setTimeout(1);
  while (!Serial){
    fadeLoop(LED, IN_OUT);
  }
  Serial.println("Serial connected successfully");
  checkPassword(password, passwordAttempts);
}

void loop() {

  Serial.println("Selct an action to perform:");
  for(int x = 1; x < CMD_LIST_SIZE; x++){
    Serial.print(x);
    Serial.print(": ");
    Serial.println(CMD_LIST[x - 1]);
  }
  waitForInput();
  int i = Serial.parseInt();
  if(i == 0){
    return;
  } else if(i < CMD_LIST_SIZE && i > 0){
    switch(i){
      //Update
      case 1:
        updateTask();
        break;
      //Blink
      case 2:
        fadeLoop(LED, IN_OUT);
        break;
      //Strobe
      case 3:
        int cycles;
        Serial.println("Input number of cycles to comple.\n-1 will loop untill further input is recieved.\nAny input will interrupt the strobe.");
        waitForInput();
        cycles = Serial.parseInt();
        if(cycles > 0){
          for(int i = cycles; i > 0; i--){
            if(Serial.available() != 0){
              Serial.readString();
              Serial.println("Strobe interrupted.");
              return;
            }
            Serial.print(i);
            Serial.println(" cycles remaining");
            fadeLoop(LED, IN_OUT);
          }
        } else if(cycles == -1){
          Serial.println("Blinking untill further notice.");
          while(Serial.available() == 0)
            fadeLoop(LED, IN_OUT);
        } else {
          Serial.print(cycles);
          Serial.println(" is not an acceptable number of cycles");
        }
        break;
      //Lock
      case 4:
        checkPassword(password, passwordAttempts);
    }
  }
}

void checkPassword(String password, int numTries){
  if(password == "")
    passwordFailed();
  String passIn = "";
  waitForInput("Please enter your password", 10000);
  passIn = Serial.readString();
  while (passIn != password && numTries > 1){
    Serial.print("Incorrect password. Attempts remaining: ");
    Serial.println(numTries--);
    Serial.println("Please enter your password");
    waitForInput();
    passIn = Serial.readString();
  }
  if(passIn == password){
    Serial.println("Password correct");
  } else {
    Serial.println("Incorrect password. THIS IS YOUR FINAL ATTEMPT.");
    Serial.println("Please enter your password");
    while(Serial.available() == 0){
      ;
    }
    passIn = Serial.readString();
    if(passIn == password){
      Serial.println("Password correct");
    } else {
      passwordFailed();
    }
  }
}

void passwordFailed(){
  for(int ad = 0; ad < MAX_PROM; ad++){
    EEPROM.write(ad, '\0');
  }
  while(true){
    Serial.println("You didn't say the magic word");
  }
}

void updateTask(){
  Serial.println("Select variable to update:");
  for(int x = 1; x < UPDATE_LIST_SIZE; x++){
    Serial.print(x);
    Serial.print(": ");
    Serial.println(UPDATE_LIST[x - 1]);
  }
  waitForInput();
  int temp = Serial.parseInt();
  if(temp == 0){
    return;
  } else if(temp < UPDATE_LIST_SIZE && temp > 0){
    Serial.print("Input a new value for: ");
    Serial.print(UPDATE_LIST[temp - 1]);
  }
  switch(temp){
    //Period
    case 1:
      int newPeriod;
      Serial.println(" between 1 and 10,000. Default: 2000");
      waitForInput();
      newPeriod = Serial.parseInt();
      if(newPeriod > 0 && newPeriod <= 10000){
        period = newPeriod;
        EEPROM.write(MAX_PROM - 1, (period / 10));
      } else {
        Serial.print(newPeriod);
        Serial.println(" is not an acceptable value.");
      }
      break;
    //Password
    case 2:
      waitForInput();
      String newPass = Serial.readString();
      newPass.trim();
      updatePassword(password.length(), newPass);
      Serial.println("Password successfully updated.");
      break;
    }
}

void updatePassword(int oldPassLength, String newPassword){
  //Clear old password
  for(int n = 0; n < oldPassLength; n++){
    EEPROM.write(MAX_PROM - n, '\0');
  }
  //Write in new one
  int a = MAX_PROM;
  int b = 0;
  while(newPassword.charAt(b) != NULL){
    EEPROM.write(a--, newPassword.charAt(b++));
  }
  password = newPassword;
}

void waitForInput(){
  while(Serial.available() == 0){
    ;
  }
}

void waitForInput(String message, int delayTime){
  long goalTime = millis() + delayTime;
  Serial.println(message);
  while(Serial.available() < 1){
    if(millis() == goalTime){
      goalTime+=delayTime;
      Serial.println(message);
    }
  }
}

void fadeLoop(int pwmPin, boolean dir){
  startTime = millis();
  sysTime = millis();
  while(sysTime < startTime + period){
    if(dir)
      ledValue = 128+127*cos(2*PI/period*((sysTime - startTime)));
    else
      ledValue = 128-127*cos(2*PI/period*((sysTime - startTime)));
    analogWrite(LED, ledValue);
    sysTime = millis();
  }
}

void fadeOneWay(int pwmPin, boolean dir){
  startTime = millis();
  sysTime = millis();
  while(sysTime < startTime + period){
    if(dir)
      ledValue = 128+127*cos(2*PI/period*((sysTime - startTime)));
    else
      ledValue = 128-127*cos(2*PI/period*((sysTime - startTime)));
    analogWrite(LED, ledValue);
    sysTime = millis();
  }
}
