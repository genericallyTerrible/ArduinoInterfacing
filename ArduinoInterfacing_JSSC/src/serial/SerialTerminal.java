/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author John
 */
public class SerialTerminal extends javax.swing.JFrame {

    //<editor-fold defaultstate="collapsed" desc="Constants">
    private final String FRAME_TITLE          = "Serial Terminal v0.1";
    private final String USER_MESSAGE_PREFACE = "--> "; //Printed out to designate a user sent message
    private final String CONNECT              = "Connect";
    private final String DISCONNECT           = "Disconnect";
    private final char   ECHO_CHAR            = '*';

    private final Color SHADOW = new Color(160, 160, 160);
    private final Color defaultColor = new Color(0, 0, 0);

    private final int MAX_GRAPH_POINTS = 100; //Points to be displayed on graph

    private final int BAUD_RATES[] = {300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200, 230400, 250000};
    private final int DEFAULT_BAUD_INDEX = 5; //9600

    private final ActionListener REFRESH_LISTENER;
    private final Timer REFRESH_TIMER;
    private final int TIMER_DELAY = 1000; //Time of delay between refreshes in milliseconds
    private final SerialPortEventListener SERIAL_PORT_READER;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Class variables">
    private String[] portList;
    private SerialPort connectedPort;
    private TermLog terminalLog;
    private LineChart dataChart;
    //</editor-fold>

    /**
     * Creates new form jSSC_SerialFrame
     */
    public SerialTerminal() {
        //<editor-fold defaultstate="collapsed" desc="Code to be executed upon window close">
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(connectedPort != null)        //If there was a port connection at any time
                    if(connectedPort.isOpened()) //And it was left open
                        try {                    //Attempt to close it
                            connectedPort.closePort();
                            System.out.println("Closed the open port");
                        } catch (SerialPortException ex) {
                            Logger.getLogger(SerialTerminal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                e.getWindow().dispose();
            }
        });
        //</editor-fold>
        initComponents();
        //<editor-fold defaultstate="collapsed" desc="Component Setup">

        //Create and add the terminal log
        terminalLog = new TermLog();
        terminalLog.setVisible(true);
        terminal_JPanel.add(terminalLog, BorderLayout.CENTER);

        //Create the line chart
        dataChart = new LineChart("Data in", "Samples", "Value", MAX_GRAPH_POINTS);

        //Set the frame title
        this.setTitle(FRAME_TITLE);

        //Setup the password field to display it's text (treat it like a regualr input)
        input_JPass.setEchoChar((char)0);

        //Populate the portList combo box
        portList = SerialPortList.getPortNames();
        if (portList.length > 0) {
            portList_JCombo.removeAllItems();
            for (String port : portList) {
                portList_JCombo.addItem(port);
            }
            buttonSetAvailableConnections();
        }

        //Populate buad rates combo box
        for(int rate : BAUD_RATES){
            buadRates_JCombo.addItem(rate + " baud");
        }
        buadRates_JCombo.setSelectedIndex(DEFAULT_BAUD_INDEX);

        //Set the carret postion of the output to autoscroll
        terminalLog.setAutoscroll(true);

        //Create the Serial port reader.
        //<editor-fold defaultstate="collapsed" desc="Serial Port Reader">
        SERIAL_PORT_READER = (SerialPortEvent event) -> {
            if (event.isRXCHAR()) {//If data is available
                if (event.getEventValue() > 0) {//Check bytes count in the input buffer
                    try {
                        byte buffer[] = connectedPort.readBytes(event.getEventValue());
                        String strIn = new String(buffer);
                        terminalLog.append(strIn);
                        if(isInteger(strIn.trim(), 10))
                            dataChart.append(Integer.parseInt(strIn.trim()));
                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                }
            } else if (event.isCTS()) {//If CTS line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("CTS - ON");
                } else {
                    System.out.println("CTS - OFF");
                }
            } else if (event.isDSR()) {///If DSR line has changed state
                if (event.getEventValue() == 1) {//If line is ON
                    System.out.println("DSR - ON");
                } else {
                    System.out.println("DSR - OFF");
                }
            } else if(event.isBREAK()){
                System.out.println("BREAK");
            } /*else {
            System.out.println("No data found.");
            }*/
        };
        //</editor-fold>


        //Setup for the timer
        //<editor-fold defaultstate="collapsed" desc="Refresh Listener">
        REFRESH_LISTENER = (ActionEvent e) -> {
            String[] lastList = portList; //Save the port list's current setup
            Object selectedItem = portList_JCombo.getSelectedItem(); //Save the user selcted item
            portList = SerialPortList.getPortNames(); //Update portList
            if (portList.length > 0) { //So long as there is an available port
                if(!Arrays.equals(lastList, portList)){ //If the list of ports has changed
                    portList_JCombo.removeAllItems(); //removed all the previous ports
                    for (String port : portList) { //Add in all the new ports
                        portList_JCombo.addItem(port);
                    }
                    //Since the port list combo box is non-editable, this will
                    //set the selected item to be the previously selected port
                    //or be ignored if it isn't in the list.
                    portList_JCombo.setSelectedItem(selectedItem);
                }
                if(CONNECT.equals(connect_JButton.getText())){ //If not currently connected
                    buttonSetAvailableConnections(); //show that there are available connections
                }
            } else { //No available ports found
                enableInterface(false); //Disable all buttons (asside from clear)
                if(connect_JButton.getText().equals(DISCONNECT)){ //If there was previously an open connection
                    connect_JButton.setText(CONNECT); //Reset the text to display no connection
                    input_JPass.setCaretPosition(0); //Effectively clears any selected text in the input
                    //Show the user that there was a forced disconnection
                    JOptionPane.showMessageDialog(rootPane,
                            "Error: Forced disconnection at " + portList_JCombo.getSelectedItem(),
                            "Forced disconnection",
                            JOptionPane.OK_OPTION,
                            null);
                }

            }
        };
        //</editor-fold>

        //Create the refresh timer
        REFRESH_TIMER = new Timer(TIMER_DELAY, REFRESH_LISTENER);
        REFRESH_TIMER.setRepeats(true);
        REFRESH_TIMER.start();
        //</editor-fold>
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connect_JPanel = new javax.swing.JPanel();
        portList_JCombo = new javax.swing.JComboBox();
        buadRates_JCombo = new javax.swing.JComboBox();
        connect_JButton = new javax.swing.JButton();
        clear_JButton = new javax.swing.JButton();
        terminal_JPanel = new javax.swing.JPanel();
        send_JPanel = new javax.swing.JPanel();
        input_JPass = new javax.swing.JPasswordField();
        send_JButton = new javax.swing.JButton();
        autoscroll_JCheck = new javax.swing.JCheckBox();
        hideInput_JCheck = new javax.swing.JCheckBox();
        lineEnding_JCombo = new javax.swing.JComboBox();
        showSplit_JCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Serial Terminal v0.01");
        setMinimumSize(new java.awt.Dimension(525, 343));
        setName("serialJFrame"); // NOI18N

        portList_JCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No devices found" }));
        portList_JCombo.setEnabled(false);
        portList_JCombo.setMinimumSize(new java.awt.Dimension(107, 25));
        portList_JCombo.setPreferredSize(new java.awt.Dimension(107, 25));

        buadRates_JCombo.setMaximumRowCount(9);
        buadRates_JCombo.setEnabled(false);

        connect_JButton.setText("Connect");
        connect_JButton.setEnabled(false);
        connect_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connect_JButtonActionPerformed(evt);
            }
        });

        clear_JButton.setText("Clear");
        clear_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clear_JButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connect_JPanelLayout = new javax.swing.GroupLayout(connect_JPanel);
        connect_JPanel.setLayout(connect_JPanelLayout);
        connect_JPanelLayout.setHorizontalGroup(
            connect_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connect_JPanelLayout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addComponent(portList_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(buadRates_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(connect_JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(clear_JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        connect_JPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clear_JButton, connect_JButton});

        connect_JPanelLayout.setVerticalGroup(
            connect_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connect_JPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connect_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portList_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buadRates_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connect_JButton)
                    .addComponent(clear_JButton))
                .addContainerGap())
        );

        connect_JPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {buadRates_JCombo, clear_JButton, connect_JButton, portList_JCombo});

        terminal_JPanel.setMinimumSize(new java.awt.Dimension(160, 15));
        terminal_JPanel.setName(""); // NOI18N
        terminal_JPanel.setPreferredSize(new java.awt.Dimension(160, 15));
        terminal_JPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                terminal_JPanelFocusLost(evt);
            }
        });
        terminal_JPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                terminal_JPanelMouseClicked(evt);
            }
        });
        terminal_JPanel.setLayout(new java.awt.BorderLayout());

        input_JPass.setText("Type your message here...");
        input_JPass.setEnabled(false);
        input_JPass.setPreferredSize(new java.awt.Dimension(134, 25));
        input_JPass.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                input_JPassFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                input_JPassFocusLost(evt);
            }
        });
        input_JPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input_JPassActionPerformed(evt);
            }
        });

        send_JButton.setText("Send");
        send_JButton.setEnabled(false);
        send_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                send_JButtonActionPerformed(evt);
            }
        });

        autoscroll_JCheck.setSelected(true);
        autoscroll_JCheck.setText("Autoscroll");
        autoscroll_JCheck.setEnabled(false);
        autoscroll_JCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoscroll_JCheckActionPerformed(evt);
            }
        });

        hideInput_JCheck.setText("Hide input");
        hideInput_JCheck.setEnabled(false);
        hideInput_JCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideInput_JCheckActionPerformed(evt);
            }
        });

        lineEnding_JCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No line ending", "\"/n\"", "\"/r\"", "\"/r/n\"" }));
        lineEnding_JCombo.setEnabled(false);
        lineEnding_JCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineEnding_JComboActionPerformed(evt);
            }
        });

        showSplit_JCheck.setText("Show Graph");
        showSplit_JCheck.setEnabled(false);
        showSplit_JCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSplit_JCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout send_JPanelLayout = new javax.swing.GroupLayout(send_JPanel);
        send_JPanel.setLayout(send_JPanelLayout);
        send_JPanelLayout.setHorizontalGroup(
            send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, send_JPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(send_JPanelLayout.createSequentialGroup()
                        .addComponent(autoscroll_JCheck)
                        .addGap(18, 18, 18)
                        .addComponent(hideInput_JCheck)
                        .addGap(18, 18, 18)
                        .addComponent(showSplit_JCheck)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(input_JPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(send_JButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lineEnding_JCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        send_JPanelLayout.setVerticalGroup(
            send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(send_JPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(input_JPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(send_JButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(send_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lineEnding_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoscroll_JCheck)
                    .addComponent(hideInput_JCheck)
                    .addComponent(showSplit_JCheck))
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(terminal_JPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(send_JPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(connect_JPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(connect_JPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(terminal_JPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(send_JPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static boolean isInteger(String s, int radix) {
    if(s.isEmpty()) return false;
    for(int i = 0; i < s.length(); i++) {
        if(i == 0 && s.charAt(i) == '-') {
            if(s.length() == 1) return false;
            else continue;
        }
        if(Character.digit(s.charAt(i),radix) < 0) return false;
    }
    return true;
}

    // <editor-fold defaultstate="collapsed" desc="Connect Button and createConnection()">
    private void connect_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connect_JButtonActionPerformed
        if(CONNECT.equals(connect_JButton.getText())){ //If there are no current connections
            String desiredPort = (String) portList_JCombo.getSelectedItem(); //Attempt to connect
            connectedPort = new SerialPort(desiredPort);                     //given the user's
            int baudRate = BAUD_RATES[buadRates_JCombo.getSelectedIndex()];  //input
            if(createConnection(connectedPort, baudRate)){ //If the connection was successfull
                buttonSetConnected();
                connect_JButton.setText(DISCONNECT);
                input_JPass.requestFocus();
                input_JPass.selectAll();
            } //Failed connections are handled alread in the createConnection method
        } else { //There is a current connection
            try { //Try to close the port and set the frame to display there currently isn't a connection
                connectedPort.closePort();
                connect_JButton.setText(CONNECT);
                buttonSetAvailableConnections();
            } catch (SerialPortException ex) {
                //Show the user there was an error disconnecting
                JOptionPane.showMessageDialog(rootPane,
                            "Error: Unable to disconnect from " + portList_JCombo.getSelectedItem(),
                            "Unable to disconnect",
                            JOptionPane.OK_OPTION,
                            null);
                System.out.println(ex); //Print out the exception
            }
        }
    }//GEN-LAST:event_connect_JButtonActionPerformed
    /** Will attempt to create a serial connection to the chosen port at the desired baud rate */
    private boolean createConnection(SerialPort chosenPort, int baudRate){
        try { //Attempt to open a port
            chosenPort.openPort(); //Open the port
            chosenPort.setParams(baudRate, //Set it's parameters
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            chosenPort.addEventListener(SERIAL_PORT_READER); //Attach our listener
            return true; //Return that the connection was successful
        } catch (SerialPortException ex) { //An unsuccessful connection
            System.out.println(ex); //Print out the exception
            switch (ex.getExceptionType()) { //Display the reason as to why the connection failed
                case SerialPortException.TYPE_PORT_BUSY:
                    JOptionPane.showMessageDialog(rootPane, "Error: " + chosenPort.getPortName() + " is busy.", "Port Busy", JOptionPane.OK_OPTION, null);
                    break;
                case SerialPortException.TYPE_PERMISSION_DENIED:
                    JOptionPane.showMessageDialog(rootPane, "Error: Permission denied to" + chosenPort.getPortName(), "Permission Denied", JOptionPane.OK_OPTION, null);
                    break;
            }
            return false; //Return that the connection failed
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Clear Button">
    private void clear_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_JButtonActionPerformed
        // TODO add your handling code here:
        if(!terminalLog.isEmpty())
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to clear the terminal log?",
                    "Clear entries?",
                    JOptionPane.YES_NO_OPTION)){
                terminalLog.clearText();
                dataChart.clear();
            }
    }//GEN-LAST:event_clear_JButtonActionPerformed
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Terminal Inputs">
    private void send_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_send_JButtonActionPerformed
        passInput();
    }//GEN-LAST:event_send_JButtonActionPerformed

    private void input_JPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_input_JPassActionPerformed
        passInput();
    }//GEN-LAST:event_input_JPassActionPerformed

    /** Default function to be called when attempting to pass user input to connected device */
    private void passInput() {
        updateAutoscroll();
        String strToSee = String.valueOf(input_JPass.getPassword());
        String strToSend = strToSee;
        if(strToSee.equals("") && (lineEnding_JCombo.getSelectedIndex() == 0)){
            //Prompt about sending an empty string
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(rootPane, "You've entered nothing. Would you like to select a line ending?",
                    "Nothing entered",
                    JOptionPane.YES_NO_OPTION)){
                lineEnding_JCombo.requestFocus();
                lineEnding_JCombo.showPopup();
            }
        } else {
            if(strToSee.equals(""))
                strToSee = (String)lineEnding_JCombo.getSelectedItem();
            input_JPass.setText("");
            int lineEndingChoice = lineEnding_JCombo.getSelectedIndex();
            switch (lineEndingChoice) {
                case 0:
                    break;
                case 1:
                    strToSend += "\n";
                    break;
                case 2:
                    strToSend += "\r";
                    break;
                case 3:
                    strToSend += "\r\n";
                    break;
            }
            if(connectedPort.isOpened()){
                try {
                    connectedPort.writeString(strToSend);
                    if(hideInput_JCheck.isSelected()){
                        String replacement = "";
                        for(int i = 0; i < strToSee.length(); i++){
                            replacement += ECHO_CHAR;
                        }
                        strToSee = replacement;
                    }
                    terminalLog.appendOnNewLine(USER_MESSAGE_PREFACE + strToSee + "\n");
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Password field events">
    private void input_JPassFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_input_JPassFocusLost
        input_JPass.setForeground(SHADOW);
    }//GEN-LAST:event_input_JPassFocusLost

    private void input_JPassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_input_JPassFocusGained
        input_JPass.setForeground(defaultColor);
    }//GEN-LAST:event_input_JPassFocusGained

    private void hideInput_JCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideInput_JCheckActionPerformed
        if(hideInput_JCheck.isSelected()){
            input_JPass.setEchoChar(ECHO_CHAR);
        } else {
            input_JPass.setEchoChar((char)0);
        }
        input_JPass.requestFocus();
    }//GEN-LAST:event_hideInput_JCheckActionPerformed
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Autoscroll actions">
    private void autoscroll_JCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoscroll_JCheckActionPerformed
        updateAutoscroll();
    }//GEN-LAST:event_autoscroll_JCheckActionPerformed

    private void terminal_JPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_terminal_JPanelMouseClicked
        if(DISCONNECT.equals(connect_JButton.getText()))
        updateAutoscroll();
    }//GEN-LAST:event_terminal_JPanelMouseClicked

    private void terminal_JPanelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_terminal_JPanelFocusLost
        updateAutoscroll();
    }//GEN-LAST:event_terminal_JPanelFocusLost

    private void lineEnding_JComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineEnding_JComboActionPerformed
        input_JPass.requestFocus();
    }//GEN-LAST:event_lineEnding_JComboActionPerformed

   private void updateAutoscroll() {
        terminalLog.setAutoscroll(autoscroll_JCheck.isSelected());
    }
    //</editor-fold>

    private void showSplit_JCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSplit_JCheckActionPerformed
        terminal_JPanel.removeAll();
        //Create the split view terminal for possible usage
        SplitTerminal splitView = new SplitTerminal(terminalLog, dataChart, true);
        if(showSplit_JCheck.isSelected()) {
            terminal_JPanel.add(splitView, BorderLayout.CENTER);
        } else {
            terminal_JPanel.add(terminalLog, BorderLayout.CENTER);
        }
        terminal_JPanel.revalidate();
        terminal_JPanel.repaint();
    }//GEN-LAST:event_showSplit_JCheckActionPerformed

    //<editor-fold defaultstate="collapsed" desc="Button States">
    /** Changes the state of all buttons, except clear, which shall always be enabled */
    private void enableInterface(boolean newState) {
        portList_JCombo.setEnabled(newState);
        buadRates_JCombo.setEnabled(newState);
        connect_JButton.setEnabled(newState);
        clear_JButton.setEnabled(true);
        input_JPass.setEnabled(newState);
        autoscroll_JCheck.setEnabled(newState);
        hideInput_JCheck.setEnabled(newState);
        showSplit_JCheck.setEnabled(newState);
        send_JButton.setEnabled(newState);
        lineEnding_JCombo.setEnabled(newState);
    }
    /** Default button states for when connections are available */
    private void buttonSetAvailableConnections() {
        portList_JCombo.setEnabled(true);
        buadRates_JCombo.setEnabled(true);
        connect_JButton.setEnabled(true);
        input_JPass.setEnabled(false);
        autoscroll_JCheck.setEnabled(false);
        hideInput_JCheck.setEnabled(false);
        showSplit_JCheck.setEnabled(false);
        send_JButton.setEnabled(false);
        lineEnding_JCombo.setEnabled(false);
    }

    /** Default button states for when connected to a device */
    private void buttonSetConnected() {
        portList_JCombo.setEnabled(false);
        buadRates_JCombo.setEnabled(false);
        connect_JButton.setEnabled(true);
        input_JPass.setEnabled(true);
        autoscroll_JCheck.setEnabled(true);
        hideInput_JCheck.setEnabled(true);
        showSplit_JCheck.setEnabled(true);
        send_JButton.setEnabled(true);
        lineEnding_JCombo.setEnabled(true);
    }
    //</editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SerialTerminal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new SerialTerminal().setVisible(true);
        });
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Varibale Declarations">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoscroll_JCheck;
    private javax.swing.JComboBox buadRates_JCombo;
    private javax.swing.JButton clear_JButton;
    private javax.swing.JButton connect_JButton;
    private javax.swing.JPanel connect_JPanel;
    private javax.swing.JCheckBox hideInput_JCheck;
    private javax.swing.JPasswordField input_JPass;
    private javax.swing.JComboBox lineEnding_JCombo;
    private javax.swing.JComboBox portList_JCombo;
    private javax.swing.JButton send_JButton;
    private javax.swing.JPanel send_JPanel;
    private javax.swing.JCheckBox showSplit_JCheck;
    private javax.swing.JPanel terminal_JPanel;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
}
