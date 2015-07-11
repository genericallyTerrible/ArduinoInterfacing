#define LED 13

//commands
const char* CMD_LIST[]     = {"PAUSE", "RESUME", "UPDATE"};
const char* UPDATE_LIST[]  = {"Period"};
const int CMD_LIST_SIZE    = sizeof(CMD_LIST);
const int UPDATE_LIST_SIZE = sizeof(UPDATE_LIST);

//directions for fadeLoop()
const boolean IN_OUT = true;
const boolean OUT_IN = !IN_OUT;

const String password = "123abc";
int passwordAttempts = 5;

byte index = 0;
int ledValue = 0;
int period = 2000;
int task = 0;
int temp = 0;
long startTime = 0;
long sysTime = 0;
String input = "";
String strIn = "\0";
boolean foundCmd = false;


void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH);
  Serial.begin(9600);
  Serial.setTimeout(1);
  while (!Serial){
    fadeLoop(LED, IN_OUT);
  }
  Serial.println("Serial connected successfully");
  checkPassword(password, passwordAttempts);
}

void loop() {
  //
    if(Serial.available() > 0){
      strIn = Serial.readString();
      strIn.trim();
      if(strIn.charAt(0) != '\0'){
        foundCmd = false;
        for(int i = 1; i < CMD_LIST_SIZE; i++){
          if(strIn == (String)CMD_LIST[i - 1]){
            foundCmd = true;
            task = i;
          }
        }
        if(!foundCmd){
          Serial.print("\"");
          Serial.print(strIn);
          Serial.println("\" is not a recognized command");
          task = 0;
        }
      }
      strIn = "\0";
      switch(task){
      //Default do nothing state
      case 0:
        break;
      //PAUSE
      case 1:
        fadeLoop(LED, IN_OUT);
        break;
      //RESUME
      case 2:
        fadeLoop(LED, IN_OUT);
        task = 0;
        break;
      //UPDATE
      case 3:
        updateTask();
        break;
      }
    }
}

void checkPassword(String password, int numTries){
  String passIn = "";
  Serial.println("Please enter your password");
  waitForInput();
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
      while(true){
        Serial.println("You didn't say the magic word");
      }
    }
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
  } else if(temp < UPDATE_LIST_SIZE && temp){
    Serial.print("Input a new value for: ");
    Serial.print(UPDATE_LIST[temp - 1]);
  }
  switch(temp){
  //period
  case 1:
    Serial.println(" between 1 and 10,000.");
    waitForInput();
    int newPeriod = Serial.parseInt();
    if(newPeriod > 0 && newPeriod <= 10000){
      period = newPeriod;
    } else {
      Serial.print(newPeriod);
      Serial.println(" is not an acceptable value.");
    }
    break;
  }
}

void waitForInput(){
  while(Serial.available() == 0){
    ;
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
