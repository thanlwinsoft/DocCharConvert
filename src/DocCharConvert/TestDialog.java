/*
 * TestDialog.java
 *
 * Created on April 27, 2005, 11:54 AM
 */

package DocCharConvert;
import java.awt.Font;
import javax.swing.JOptionPane;
import DocCharConvert.Converter.*;
/**
 *
 * @author  keith
 */
public class TestDialog extends javax.swing.JDialog
{
    private ChildConverter converter = null;
    private ChildConverter reverseConverter = null;
    private int DEBUG_FONT_SIZE = 12;
    private String DEBUG_FONT = "Courier";
    /** Creates new form TestDialog */
    public TestDialog(java.awt.Frame parent, boolean modal, ChildConverter cc, ChildConverter rcc)
    {
        super(parent, modal);
        initComponents();
        converter = cc;
        reverseConverter = rcc;
        int fontSize = Config.getCurrent().getTestFontSize();
        jTextArea1.setFont(new Font(cc.getOldStyle().getFontName(),
                           Font.PLAIN,fontSize));
        jTextArea2.setFont(new Font(cc.getNewStyle().getFontName(),
                           Font.PLAIN,fontSize));
        jTextArea3.setFont(new Font(cc.getOldStyle().getFontName(),
                           Font.PLAIN,fontSize));
        jTextArea4.setFont(new Font(DEBUG_FONT,
                           Font.PLAIN,DEBUG_FONT_SIZE));
        this.setSize(600,500);
        this.setTitle("Test Conversion");
        converterLabel.setText(cc.getName());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        buttonPanel = new javax.swing.JPanel();
        converterLabel = new javax.swing.JLabel();
        convertButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jTextArea1.setLineWrap(true);
        jTextArea1.setToolTipText("Type test text here");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane1.setViewportView(jTextArea1);

        jSplitPane2.setLeftComponent(jScrollPane1);

        jTextArea2.setEditable(false);
        jTextArea2.setLineWrap(true);
        jTextArea2.setToolTipText("Result of conversion");
        jTextArea2.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTextArea2);

        jSplitPane2.setRightComponent(jScrollPane2);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setResizeWeight(0.5);
        jTextArea3.setEditable(false);
        jTextArea3.setLineWrap(true);
        jTextArea3.setToolTipText("Result of reverse conversion (round trip conversion)");
        jTextArea3.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTextArea3);

        jSplitPane3.setLeftComponent(jScrollPane3);

        jTextArea4.setEditable(false);
        jTextArea4.setLineWrap(true);
        jTextArea4.setToolTipText("Hex codes of input, output and reverse strings");
        jTextArea4.setWrapStyleWord(true);
        jScrollPane4.setViewportView(jTextArea4);

        jSplitPane3.setRightComponent(jScrollPane4);

        jSplitPane1.setRightComponent(jSplitPane3);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonPanel.add(converterLabel);

        convertButton.setText("Convert");
        convertButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                convertButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(convertButton);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                closeButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(closeButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.NORTH);

        pack();
    }//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
    {//GEN-HEADEREND:event_closeButtonActionPerformed
        // TODO add your handling code here:
        setVisible(false);
    }//GEN-LAST:event_closeButtonActionPerformed

    private void convertButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_convertButtonActionPerformed
    {//GEN-HEADEREND:event_convertButtonActionPerformed
        // TODO add your handling code here:
        
        StringBuffer debugText = new StringBuffer();
        ChildConverter cc = converter;
        try
        {
            cc.initialize();
            String output = cc.convert(jTextArea1.getText());
            jTextArea2.setText(output);
            CharConverter rcc = reverseConverter;
            if (rcc != null)
            {
                rcc.initialize();
                String reverse = rcc.convert(output);
                jTextArea3.setText(reverse);
                rcc.destroy();
            }
            else System.out.println("no reverse converter found");
            cc.destroy();
            debugDump(jTextArea1.getText(),debugText);
            debugDump(jTextArea2.getText(),debugText);
            debugDump(jTextArea3.getText(),debugText);
            jTextArea4.setText(debugText.toString());
        }
        catch (CharConverter.FatalException e)
        {              
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        catch (CharConverter.RecoverableException e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        
    }//GEN-LAST:event_convertButtonActionPerformed
    
    protected void debugDump(String text, StringBuffer buffer)
    {
        for (int i = 0; i<text.length(); i++)
        {
            String hex = Integer.toHexString(text.charAt(i));
            if (hex.length() == 2) buffer.append("  ");
            buffer.append(hex);
            buffer.append(" ");
        }
        buffer.append("\n");
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton convertButton;
    private javax.swing.JLabel converterLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JTextArea jTextArea4;
    // End of variables declaration//GEN-END:variables
    
}