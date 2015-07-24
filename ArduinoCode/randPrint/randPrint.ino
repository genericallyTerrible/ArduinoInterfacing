int randInt = 0;
int delayTime = 10;
void setup() {
  randomSeed(analogRead(0));
  // put your setup code here, to run once:
  Serial.begin(9600);
  while (!Serial) {
    ;
  }
  Serial.println("Serial connected successfully");
}

void loop() {
  // put your main code here, to run repeatedly:
  randInt = random(10);
  Serial.println(randInt);
  delay(delayTime);
  if(Serial.available() > 0){
    Serial.read();
  }
}
