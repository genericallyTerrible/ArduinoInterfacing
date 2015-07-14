/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serial;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author John
 */
public class TermLog extends JScrollPane {

    /**
     * Creates new form TermLog
     */
    public TermLog() {
        initComponents();
    }

    /**
     * Adds a String to the end of the JTextArea
     * @param message input string to be added
     */
    public void append(String message) {
        textArea.append(message);
    }

    /**
     * Adds a string on a new line at the end of the JTextArea
     * @param message input string to be added on a new line
     */
    public void appendOnNewLine(String message) {
        try {
            int lastLine = textArea.getLineCount() - 1;
            int beginOfLastLine = textArea.getLineStartOffset(lastLine);
            int endOfLastLine = textArea.getLineEndOffset(lastLine);
            if (beginOfLastLine != endOfLastLine) {
                textArea.append("\n");
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(SerialTerminal.class.getName()).log(Level.SEVERE, null, ex);
        }
        textArea.append(message);
    }
    
    /**
     * Clears the JTextArea
     */
    public void clearText(){
        textArea.setText("");
    }
    
    /**
     * Gets the entire content of the JTextArea as a string
     * @return content of the JTextArea
     */
    public String getText(){
        return textArea.getText();
    }
    
    /**
     * Checks if the JTextArea is empty or not
     * @return True if empty, false otherwise
     */
    public boolean isEmpty(){
        return(textArea.getText().equals(""));
    }
    
    /**
     * Gets the caret of the JTextArea
     * @return the caret of the JTextArea
     */
    public Caret getCaret(){
        return textArea.getCaret();
    }

    /**
     * Enables or disables keeping the caret at the end of the document,
     * effectively scrolling to the bottom of the text area automatically while enabled.
     * @param enable True enables, false disables
     */
    public void setAutoscroll(boolean enable){
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        if(enable){
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        } else {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);    
        }
    }
    
    /**
     * Places the caret at the end of the text area.
     * Does not modify the caret's behavior.
     */
    public void placeCaretAtEnd(){
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textArea = new javax.swing.JTextArea();

        textArea.setEditable(false);
        textArea.setColumns(15);
        setViewportView(textArea);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
