#define LED 13
const int delayTime = 1000;
int thisTime = 0;
int lastTime = 0;
int difference = 0;
int drift = 0;
String difDir = "";
String output = "";
String driftStr = "";

void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
  Serial.begin(9600);
  while (!Serial){
    ;//Wait for serial connection; Needed only for Leonardo
  }
  Serial.println("Serial connected successfully");
  thisTime = millis() - delayTime;
}

void loop() {
  lastTime = thisTime;
  thisTime = millis();
  difference = thisTime - (lastTime + delayTime);
  Serial.print(millis());
  if(difference != 0){
    drift += difference;
    if(difference > 0){
      difDir = "Gained";
    } else {
      difDir = "Lost";
      difference *= -1;
    }
    output = ": " + difDir + " " + difference + " miliseconds";
  } else {
    output = ": No time gained or lost";
  }
  driftStr = " Overall drift of: ";
  driftStr += drift;
  Serial.print(output);
  Serial.println(driftStr);
  delay(delayTime);
}

/*
  digitalWrite(LED, HIGH);
  delay(500);
  digitalWrite(LED, LOW);
  delay(500);
*/
