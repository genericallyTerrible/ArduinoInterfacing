#include <EEPROM.h>
void setup(){

  String password = "";

  char input = '\0';
  int address = 511;
  input = EEPROM.read(address--);
  while (input != '\0'){
    password += input;
    input = EEPROM.read(address--);
  }

  Serial.begin(9600);
  while (!Serial){
    ;
  }
  Serial.println(password);
  Serial.println(EEPROM.read(0));
  Serial.println(EEPROM.read(1));
}

void loop(){

}
