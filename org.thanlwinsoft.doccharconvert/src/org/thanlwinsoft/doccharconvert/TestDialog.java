/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.AbstractAction;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
/**
 *
 * @author  keith
 */
public class TestDialog extends javax.swing.JDialog
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3152816015516734437L;
	private ChildConverter converter = null;
    private ChildConverter reverseConverter = null;
    private int DEBUG_FONT_SIZE = 12;
    private String DEBUG_FONT = "Courier";
    private ResourceBundle guiResource = null;
    /** Creates new form TestDialog */
    public TestDialog(MainForm parent, boolean modal, ChildConverter cc, ChildConverter rcc)
    {
        super(parent, modal);
        guiResource = parent.getResource();
        initComponents();
        converter = cc;
        reverseConverter = rcc;
        debugCheckBox = new JCheckBox();
        debugCheckBox.setSelected(false);
        
        debugCheckBox.setText(guiResource.getString("enable_debug"));
        debugCheckBox.setToolTipText(guiResource.getString("enable_debug_tt"));
        
        debugCheckBox.addActionListener(new java.awt.event.ActionListener() { 
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
              debugCheckBoxActionPerformed(evt);
            }
        });
        buttonPanel.add(debugCheckBox, 1); // add after label
        // set debug in this mode - for now output is written to console
        cc.setDebug(false, null);
        rcc.setDebug(false, null);
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
        this.setTitle(guiResource.getString("test_dialog_title"));
        converterLabel.setText(cc.getName());
        initContextMenu();
    }
    
    protected JTextComponent getTextComponentFromAction(ActionEvent ae)
    {
        if (ae.getSource() instanceof JMenuItem)
        {
            JMenuItem mi = (JMenuItem)ae.getSource();
            if (mi.getParent() instanceof JPopupMenu)
            {
                JPopupMenu pm = (JPopupMenu)mi.getParent();
                if (pm.getInvoker() instanceof JTextComponent)
                {
                    return (JTextComponent)pm.getInvoker();
                }
            }
        }
        return null;
    }
    
    private void initContextMenu()
    {
        JPopupMenu menu = new JPopupMenu(guiResource.getString("popup_title"));
        JPopupMenu readOnlyMenu = new JPopupMenu(guiResource.getString("popup_title"));
        AbstractAction copyAction = new AbstractAction(guiResource.getString("copy")) 
        {
            /**
             * 
             */
            private static final long serialVersionUID = -5237671201534677052L;

            public void actionPerformed(ActionEvent ae)
            {
                JTextComponent tc = getTextComponentFromAction(ae);
                if (tc != null) tc.copy();
            }            
        };
        AbstractAction cutAction = new AbstractAction(guiResource.getString("cut")) 
        {
            /**
             * 
             */
            private static final long serialVersionUID = 7468679584634147672L;

            public void actionPerformed(ActionEvent ae)
            {
                JTextComponent tc = getTextComponentFromAction(ae);
                if (tc != null) tc.cut();
            }            
        };
        AbstractAction pasteAction = new AbstractAction(guiResource.getString("paste")) 
        {
            /**
             * 
             */
            private static final long serialVersionUID = -2612076764097953750L;

            public void actionPerformed(ActionEvent ae)
            {
                JTextComponent tc = getTextComponentFromAction(ae);
                if (tc != null) tc.paste();
            }            
        };
        menu.add(new JMenuItem(cutAction));
        menu.add(new JMenuItem(copyAction));
        menu.add(new JMenuItem(pasteAction));
        readOnlyMenu.add(new JMenuItem(copyAction));
        jTextArea1.setComponentPopupMenu(menu);
        jTextArea2.setComponentPopupMenu(readOnlyMenu);
        jTextArea3.setComponentPopupMenu(readOnlyMenu);
        jTextArea4.setComponentPopupMenu(readOnlyMenu);
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
          JTextArea area = new JTextArea(e.getMessage());
          area.setLineWrap(true);
          area.setWrapStyleWord(true);
          JScrollPane msgPane = new JScrollPane(area);
          msgPane.setPreferredSize(new Dimension(400,300));
          JOptionPane.showMessageDialog(this, msgPane);
        }
        catch (CharConverter.RecoverableException e)
        {
          JTextArea area = new JTextArea(e.getMessage());
          area.setLineWrap(true);
          area.setWrapStyleWord(true);
          JScrollPane msgPane = new JScrollPane(area);
          msgPane.setPreferredSize(new Dimension(400,300));
            JOptionPane.showMessageDialog(this, 
              msgPane);
        }
        
    }//GEN-LAST:event_convertButtonActionPerformed
    
    private void debugCheckBoxActionPerformed(java.awt.event.ActionEvent evt)
    {
        converter.setDebug(debugCheckBox.isSelected(), null);
        reverseConverter.setDebug(debugCheckBox.isSelected(), null);
    }
    
    protected void debugDump(String text, StringBuffer buffer)
    {
        for (int i = 0; i<text.length(); i++)
        {
            String hex = Integer.toHexString(text.charAt(i));
            if (hex.length() < 4) 
            {
              int j = 4 - hex.length();
              while (j-- > 0)
                buffer.append(' ');
            }
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
  private javax.swing.JCheckBox debugCheckBox;
}
