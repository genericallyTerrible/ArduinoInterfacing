#include <EEPROM.h>
#define PASSWORD_START 0
#define PASSWORD_END 20

void setup(){
  char password[] = "123456";
  int i = 511;
  int j = 0;
  while(password[j] != '\0'){
    EEPROM.write(i--, password[j++]);
  }
  EEPROM.write(i, '\0');
  EEPROM.write(0, 0);
  EEPROM.write(1, 200);
}

void loop(){

}
