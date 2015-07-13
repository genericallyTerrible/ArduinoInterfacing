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
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
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
    
    public static SerialPort connectedPort;

    private final String FRAME_TITLE = "Serial Terminal v0.1";
    private final String USER_MESSAGE_PREFACE = "--> ";
    private final String CONNECT = "Connect";
    private final String DISCONNECT = "Disconnect";
    private final char ECHO_CHAR = '*';
    
    private final Color SHADOW = new Color(160, 160, 160);
    //private final Color caretLine = new Color(51, 51, 51);
    private final Color defColor = new Color(0, 0, 0);

    private final int MAX_GRAPH_POINTS = 1000; //Points to be displayed on graph
    private final int TIMER_DELAY = 1000; //Time of delay between refreshes in milliseconds

    private String[] portList;
    private final int BAUD_RATES[] = {300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200, 230400, 250000};
    private final int DEFAULT_BAUD_INDEX = 5; //9600

    //Action for the refreshTimer to perform
    private final ActionListener REFRESH_LISTENER;
    //Creation of the refreshTimer
    private final Timer REFRESH_TIMER;
    //Creation of serialPortReader event listener
    private final SerialPortEventListener SERIAL_PORT_READER;
    
    private TermLog terminalLog;

    /**
     * Creates new form jSSC_SerialFrame
     */
    public SerialTerminal() {
        //Attempt to garuntee a closed connection upon window close
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(connectedPort != null)
                    if(connectedPort.isOpened())
                        try {
                            connectedPort.closePort();
                            System.out.println("Closed the open port");
                        } catch (SerialPortException ex) {
                            Logger.getLogger(SerialTerminal.class.getName()).log(Level.SEVERE, null, ex);
                        }
                e.getWindow().dispose();
            }
        });

        initComponents();
        
        //Instantiate and add the terminal log
        this.terminalLog = new TermLog();
        recievePannel_JPannel.add(terminalLog, BorderLayout.CENTER);
        
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
        //LinePainter linePainter = new LinePainter(recieved_JTextArea, caretLine);

        //Setup for the timer
        //<editor-fold defaultstate="collapsed" desc="Serial Port Reader">
        SERIAL_PORT_READER = (SerialPortEvent event) -> {
            if (event.isRXCHAR()) {//If data is available
                if (event.getEventValue() > 0) {//Check bytes count in the input buffer
                    try {
                        byte buffer[] = connectedPort.readBytes(event.getEventValue());
                        String strIn = new String(buffer);
                        terminalLog.append(strIn);
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
        
        
        //<editor-fold defaultstate="collapsed" desc="Refresh Listener">
        REFRESH_LISTENER = (ActionEvent e) -> {
            String[] lastList = portList;
            String selectedItem = (String)portList_JCombo.getSelectedItem();
            portList = SerialPortList.getPortNames();
            if (portList.length > 0) {
                if(!Arrays.equals(lastList, portList)){
                    portList_JCombo.removeAllItems();
                    for (String port : portList) {
                        portList_JCombo.addItem(port);
                    }
                    portList_JCombo.setSelectedItem(selectedItem);
                }
                if(CONNECT.equals(connect_JButton.getText())){
                    buttonSetAvailableConnections();
                }
            } else {
                enableInterface(false);
                if(connect_JButton.getText().equals(DISCONNECT)){
                    connect_JButton.setText(CONNECT);
                    input_JPass.setCaretPosition(0);
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
        //Start the timer for refreshing connections
        REFRESH_TIMER.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connect_JPannel = new javax.swing.JPanel();
        portList_JCombo = new javax.swing.JComboBox();
        buadRates_JCombo = new javax.swing.JComboBox();
        connect_JButton = new javax.swing.JButton();
        clear_JButton = new javax.swing.JButton();
        recievePannel_JPannel = new javax.swing.JPanel();
        send_JPannel = new javax.swing.JPanel();
        input_JPass = new javax.swing.JPasswordField();
        send_JButton = new javax.swing.JButton();
        autoscroll_JCheck = new javax.swing.JCheckBox();
        hideInput_JCheck = new javax.swing.JCheckBox();
        lineEnding_JCombo = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Serial Terminal v0.01");
        setMinimumSize(new java.awt.Dimension(500, 343));
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

        javax.swing.GroupLayout connect_JPannelLayout = new javax.swing.GroupLayout(connect_JPannel);
        connect_JPannel.setLayout(connect_JPannelLayout);
        connect_JPannelLayout.setHorizontalGroup(
            connect_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connect_JPannelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(portList_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(buadRates_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(connect_JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(clear_JButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        connect_JPannelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {clear_JButton, connect_JButton});

        connect_JPannelLayout.setVerticalGroup(
            connect_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connect_JPannelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connect_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portList_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buadRates_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connect_JButton)
                    .addComponent(clear_JButton))
                .addContainerGap())
        );

        connect_JPannelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {buadRates_JCombo, clear_JButton, connect_JButton, portList_JCombo});

        recievePannel_JPannel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                recievePannel_JPannelFocusLost(evt);
            }
        });
        recievePannel_JPannel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                recievePannel_JPannelMouseClicked(evt);
            }
        });
        recievePannel_JPannel.setLayout(new java.awt.BorderLayout());

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

        javax.swing.GroupLayout send_JPannelLayout = new javax.swing.GroupLayout(send_JPannel);
        send_JPannel.setLayout(send_JPannelLayout);
        send_JPannelLayout.setHorizontalGroup(
            send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, send_JPannelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(send_JPannelLayout.createSequentialGroup()
                        .addComponent(autoscroll_JCheck)
                        .addGap(18, 18, 18)
                        .addComponent(hideInput_JCheck)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(input_JPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(send_JButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lineEnding_JCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        send_JPannelLayout.setVerticalGroup(
            send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(send_JPannelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(input_JPass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(send_JButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(send_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lineEnding_JCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoscroll_JCheck)
                    .addComponent(hideInput_JCheck))
                .addGap(6, 6, 6))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(recievePannel_JPannel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(send_JPannel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(connect_JPannel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(connect_JPannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(recievePannel_JPannel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(send_JPannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Connect Button">
    private void connect_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connect_JButtonActionPerformed
        // TODO add your handling code here:
        if(CONNECT.equals(connect_JButton.getText())){
            String desiredPort = (String) portList_JCombo.getSelectedItem();
            connectedPort = new SerialPort(desiredPort);
            int baudRate = BAUD_RATES[buadRates_JCombo.getSelectedIndex()];
            if(createConnection(connectedPort, baudRate)){
                buttonSetConnected();
                connect_JButton.setText(DISCONNECT);
                input_JPass.requestFocus();
                input_JPass.selectAll();
            }
        } else {
            connect_JButton.setText(CONNECT);
            buttonSetAvailableConnections();
            try {
                connectedPort.closePort();
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        }
    }//GEN-LAST:event_connect_JButtonActionPerformed
    private boolean createConnection(SerialPort chosenPort, int baudRate){
        try {
            chosenPort.openPort();
            chosenPort.setParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            chosenPort.addEventListener(SERIAL_PORT_READER);
            return true;
        } catch (SerialPortException ex) {
            System.out.println(ex);
            switch (ex.getExceptionType()) {
                case SerialPortException.TYPE_PORT_BUSY:
                    JOptionPane.showMessageDialog(rootPane, "Error: " + chosenPort.getPortName() + " is busy.", "Port Busy", JOptionPane.OK_OPTION, null);
                    break;
                case SerialPortException.TYPE_PERMISSION_DENIED:
                    JOptionPane.showMessageDialog(rootPane, "Error: Permission denied to" + chosenPort.getPortName(), "Permission Denied", JOptionPane.OK_OPTION, null);
                    break;
            }
            return false;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Clear Button">
    private void clear_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clear_JButtonActionPerformed
        // TODO add your handling code here:
        if(!terminalLog.isEmpty())
            if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(rootPane, "Are you sure you want to clear the terminal log?",
                    "Clear entries?",
                    JOptionPane.YES_NO_OPTION))
                terminalLog.clearText();
    }//GEN-LAST:event_clear_JButtonActionPerformed

    // </editor-fold>

    private void send_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_send_JButtonActionPerformed
        // TODO add your handling code here:
        inputFunction();
    }//GEN-LAST:event_send_JButtonActionPerformed
    
    private void inputFunction() {
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
    
    // <editor-fold defaultstate="collapsed" desc="Autoscroll Checkbox and updateAutoscroll()">
    private void autoscroll_JCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoscroll_JCheckActionPerformed
        // TODO add your handling code here:
        updateAutoscroll();
    }//GEN-LAST:event_autoscroll_JCheckActionPerformed

    private void input_JPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_input_JPassActionPerformed
        inputFunction();
    }//GEN-LAST:event_input_JPassActionPerformed

    private void input_JPassFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_input_JPassFocusLost
        input_JPass.setForeground(SHADOW);
    }//GEN-LAST:event_input_JPassFocusLost

    private void input_JPassFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_input_JPassFocusGained
        input_JPass.setForeground(defColor);
    }//GEN-LAST:event_input_JPassFocusGained

    private void hideInput_JCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideInput_JCheckActionPerformed
        if(hideInput_JCheck.isSelected()){
            input_JPass.setEchoChar(ECHO_CHAR);
        } else {
            input_JPass.setEchoChar((char)0);
        }
        input_JPass.requestFocus();
    }//GEN-LAST:event_hideInput_JCheckActionPerformed

    private void recievePannel_JPannelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_recievePannel_JPannelMouseClicked
        if(DISCONNECT.equals(connect_JButton.getText()))
        updateAutoscroll();
    }//GEN-LAST:event_recievePannel_JPannelMouseClicked

    private void recievePannel_JPannelFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recievePannel_JPannelFocusLost
        updateAutoscroll();
    }//GEN-LAST:event_recievePannel_JPannelFocusLost

    private void lineEnding_JComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineEnding_JComboActionPerformed
        input_JPass.requestFocus();
    }//GEN-LAST:event_lineEnding_JComboActionPerformed

   private void updateAutoscroll() {
        terminalLog.setAutoscroll(autoscroll_JCheck.isSelected());
    }
    // </editor-fold>

    private void enableInterface(boolean newState) {
        portList_JCombo.setEnabled(newState);
        buadRates_JCombo.setEnabled(newState);
        connect_JButton.setEnabled(newState);
        clear_JButton.setEnabled(true);
        input_JPass.setEnabled(newState);
        autoscroll_JCheck.setEnabled(newState);
        hideInput_JCheck.setEnabled(newState);
        send_JButton.setEnabled(newState);
        lineEnding_JCombo.setEnabled(newState);
    }

    private void buttonSetAvailableConnections() {
        portList_JCombo.setEnabled(true);
        buadRates_JCombo.setEnabled(true);
        connect_JButton.setEnabled(true);
        input_JPass.setEnabled(false);
        autoscroll_JCheck.setEnabled(false);
        hideInput_JCheck.setEnabled(false);
        send_JButton.setEnabled(false);
        lineEnding_JCombo.setEnabled(false);
    }

    private void buttonSetConnected() {
        portList_JCombo.setEnabled(false);
        buadRates_JCombo.setEnabled(false);
        connect_JButton.setEnabled(true);
        input_JPass.setEnabled(true);
        autoscroll_JCheck.setEnabled(true);
        hideInput_JCheck.setEnabled(true);
        send_JButton.setEnabled(true);
        lineEnding_JCombo.setEnabled(true);
    }

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
        //</editor-fold>
        
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
    private javax.swing.JPanel connect_JPannel;
    private javax.swing.JCheckBox hideInput_JCheck;
    private javax.swing.JPasswordField input_JPass;
    private javax.swing.JComboBox lineEnding_JCombo;
    private javax.swing.JComboBox portList_JCombo;
    private javax.swing.JPanel recievePannel_JPannel;
    private javax.swing.JButton send_JButton;
    private javax.swing.JPanel send_JPannel;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>
}