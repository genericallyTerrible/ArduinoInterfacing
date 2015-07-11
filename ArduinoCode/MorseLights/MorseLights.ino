#define LED 13

//directions for fadeLoop()
const boolean IN_OUT = true;
const boolean OUT_IN = !IN_OUT;

//morse constants
const int MORSE_DOT   = 300; //Display length of a dot (ms)
const int MORSE_PAUSE = MORSE_DOT; //Delay time between elements
const int MORSE_DASH  = 3 * MORSE_DOT;
const int MORSE_CHAR  = 3 * MORSE_DOT; //End of a character
const int MORSE_WORD  = 7 * MORSE_DOT; //End of a word

int ledValue = 0;
int startTime = 0;
int sysTime = 0;
int period = 2000; //Time(ms) it takes to complete the sin wave (HIGH-LOW-HIGH or LOW-HIGH-LOW)
int displace = period/4; //To start the sine wave at a peak

String strIn = "\0";

void setup() {
  pinMode(LED, OUTPUT);
  digitalWrite(LED, HIGH);
  Serial.begin(9600);
  Serial.setTimeout(1);
  while (!Serial){
    fadeLoop(LED, IN_OUT);//Wait for serial connection; Needed only for Leonardo
  }
  Serial.println("Serial connected successfully");
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

void morseDot(){
  digitalWrite(LED, HIGH);
  delay(MORSE_DOT);
  digitalWrite(LED, LOW);
}

void morseDash(){
  digitalWrite(LED, HIGH);
  delay(MORSE_DASH);
  digitalWrite(LED, LOW);
}

void toMorse(char c){
  if (c > 96 && c < 123){
    c -= 32;
  }
  switch (c){
    case 'A':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'B':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'C':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'D':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'E':
      morseDot();
      break;
      
    case 'F':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'G':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'H':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'I':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'J':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
    case 'K':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
    
    case 'L':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'M':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'N':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'O':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'P':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'Q':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'R':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'S':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case 'T':
      morseDash();
      break;
      
    case 'U':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'V':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'W':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'X':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'Y':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case 'Z':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '0':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '1':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '2':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '3':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '4':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '5':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '6':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '7':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '8':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '9':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '.':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case ',':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case ':':
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '?':
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '\'':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '-':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '/':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '"':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '@':
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      break;
      
    case '=':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case '(':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case ')':
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDash();
      delay(MORSE_PAUSE);
      morseDot();
      delay(MORSE_PAUSE);
      morseDash();
      break;
      
    case ' ':
      delay(MORSE_WORD - MORSE_CHAR - MORSE_CHAR); //First "-MORSE_CHAR" is to account for delay of preceding character
                                                   //Second "-MORSE_CHAR" is to account for the upcoming delay at the end of this method
      break;  
          
    default:
      Serial.print("Unsupported character \'");
      Serial.print(c);
      Serial.println("\'");
  }
    Serial.print(c);
    delay(MORSE_CHAR);
}

void loop() {
  Serial.println("Input a String to be displayed out as morse code");
  digitalWrite(LED, LOW);
  while(!Serial.available()){
    ;
  }
  strIn = Serial.readString();
  strIn.trim();
  if(strIn.charAt(0) != '\0'){
    for(int i = 0; i < strIn.length(); i++){
      toMorse(strIn.charAt(i));
    }
  }
  Serial.println();
  Serial.println("End of message");
  fadeLoop(LED, IN_OUT);
}
