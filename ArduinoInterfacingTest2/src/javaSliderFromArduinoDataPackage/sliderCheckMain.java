/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaSliderFromArduinoDataPackage;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JSlider;

/**
 *
 * @author John
 */
public class sliderCheckMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        int minimum = 1;
        int maximum = 9;
        
        JFrame  window = new JFrame();
        JSlider slider = new JSlider();
        slider.setMaximum(maximum);
        slider.setMinimum(minimum);
        window.add(slider);
        window.pack();
        window.setVisible(true);
        
        SerialPort ports[] = SerialPort.getCommPorts();
        System.out.println("Select a port");
        int x = 1;
        for(SerialPort port : ports){
            System.out.println(x++ + ". " + port.getSystemPortName());
        }

        Scanner s = new Scanner(System.in);
        int chosenPort = s.nextInt();

        SerialPort port = ports[chosenPort - 1];
        port.setBaudRate(9600);
        if(port.openPort()){
            System.out.println("Successfully opened the port.");
        } else {
            System.out.println("Failed to connect to " + port.getSystemPortName());
        }

        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        Scanner data = new Scanner(port.getInputStream());
        int number = 0;
        while(data.hasNextLine()){
            try{
                number = Integer.parseInt(data.nextLine());
            } catch(Exception e) {/*Ignored*/}
            slider.setValue(number);
        }
    }
    
}


/*
SerialPort ports[] = SerialPort.getCommPorts();
System.out.println("Select a port");
int x = 1;
for(SerialPort port : ports){
    System.out.println(x++ + ". " + port.getSystemPortName());
}

Scanner s = new Scanner(System.in);
int chosenPort = s.nextInt();

SerialPort port = ports[chosenPort - 1];
port.setBaudRate(9600);
if(port.openPort()){
    System.out.println("Successfully opened the port.");
} else {
    System.out.println("Failed to connect to " + port.getSystemPortName());
}

port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

Scanner data = new Scanner(port.getInputStream());
int number = 0;
while(data.hasNextLine()){
    try{
        number = Integer.parseInt(data.nextLine());
    } catch(Exception e) {/*Ignored*//*}
}
*/