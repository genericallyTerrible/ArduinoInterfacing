/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialCommPackage;

import java.util.Scanner;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author John
 */
public class jSSCSerialComTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        
        String buffer; //Input buffer
        Scanner scanner = new Scanner(System.in); //Scanner for user input
        String[] portList = SerialPortList.getPortNames(); //All available serial ports
        
        //Prompt the user
        System.out.println("Select your desired serial port from the list below."); 
        //Display their available choices    
        for(int x = 0; x < portList.length; x++){
            System.out.println(x + ": " + portList[x]);
        }
        //Get user input
        int input = scanner.nextInt();
        //Select user desired port
        SerialPort serialPort = new SerialPort(portList[input]);
        //Attempt serial connection
        try {
            //Open the port
            System.out.println("Port opened: " + serialPort.openPort());
            //Set the port parameters
            System.out.println("Params setted: " + serialPort.setParams(9600, 8, 1, 0));
            //Send a message
            System.out.println("\"Hello World!\" writen to port: " + serialPort.writeString("Hello, World!"));
            //Wait for a response
            while(serialPort.getInputBufferBytesCount() == 0){
                ;
            }
            //Read in the response as bytes, convert to string
            buffer = new String(serialPort.readBytes(serialPort.getInputBufferBytesCount()));
            //Display response
            System.out.println("Message recived from serial port: " + buffer);
            //Close the port
            System.out.println("Port closed: " + serialPort.closePort());
        }
        catch (SerialPortException ex){
            System.out.println(ex);
        }
    }    
}
