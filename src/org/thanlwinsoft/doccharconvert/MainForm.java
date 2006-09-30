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

import java.io.File;
//import java.util.Map.Entry;
import java.text.MessageFormat;
import java.nio.charset.Charset;
import java.awt.BorderLayout;
import java.util.Map;
import java.util.Vector;
import java.util.SortedMap;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.border.TitledBorder;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
/**
 *
 * @author  keith
 */
public class MainForm extends javax.swing.JFrame
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3143912382434841346L;
	private BatchConversion conversion = null;
    private Vector availableConverters = null;
    private DefaultListModel aModel = null;
    private DefaultListModel sModel = null;
    private Timer timer = null;
    private int TIMER_DELAY = 250;
    private ResourceBundle guiResource = null;
    private ResourceBundle msgResource = null;
    private static final String DOCS_DIR = "docs";
    private String resourceBase = 
        this.getClass().getPackage().getName().replace(".","/");
    /** Creates new form MainForm */
    public MainForm()
    {
        initComponents();
        msgResource = Config.getCurrent().getMsgResource();
        try
        {
          guiResource = ResourceBundle.getBundle(resourceBase + "/GUI");
        }
        catch (java.util.MissingResourceException mre)
        {
           System.out.println(mre.getMessage());
        }
        System.out.println(resourceBase);
        helpButton = new javax.swing.JButton(guiResource.getString("btn_help"));
        helpButton.addActionListener(new java.awt.event.ActionListener()
	    {
	      public void actionPerformed(java.awt.event.ActionEvent evt)
	      {
	        helpButtonActionPerformed(evt);
	      }
	    });

        jPanel8.remove(exitButton);
        jPanel8.add(helpButton);
        jPanel8.add(exitButton);
        	    
        addConverter.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Add.png")));
        removeConverter.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Remove.png")));
        convertButton.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Convert.png")));
        testButton.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/ConvertTest.png")));
        exitButton.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Exit.png")));
        configButton.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Config.png")));
        helpButton.setIcon(new ImageIcon(getClass().getResource("/" +resourceBase + "/icons/Help.png")));
        convertButton.setText(guiResource.getString("btn_convert"));
        testButton.setText(guiResource.getString("btn_test"));
        exitButton.setText(guiResource.getString("btn_exit"));
        configButton.setText(guiResource.getString("btn_config"));
        helpButton.setToolTipText(guiResource.getString("btn_help_tt"));
        this.iFileAdd.setText(guiResource.getString("btn_fileAdd"));
        this.iFileRemove.setText(guiResource.getString("btn_fileRemove"));
        this.saveFileList.setText(guiResource.getString("btn_saveFileList"));
        this.loadFileList.setText(guiResource.getString("btn_loadFileList"));
        this.oFileButton.setText(guiResource.getString("btn_browse"));
        this.addConverter.setText(guiResource.getString("btn_add"));
        this.removeConverter.setText(guiResource.getString("btn_remove"));
        fileModePanel.setBorder(new TitledBorder(guiResource.getString("panel_fileMode")));
        availPanel.setBorder(new TitledBorder(guiResource.getString("panel_available")));
        selectedPanel.setBorder(new TitledBorder(guiResource.getString("panel_converters")));
        inputPanel.setBorder(new TitledBorder(guiResource.getString("panel_input")));
        outputPanel.setBorder(new TitledBorder(guiResource.getString("panel_output")));
        iEncPanel.setBorder(new TitledBorder(guiResource.getString("panel_inEncoding")));
        oEncPanel.setBorder(new TitledBorder(guiResource.getString("panel_outEncoding")));
        progressPanel.setBorder(new TitledBorder(guiResource.getString("panel_progress")));
        
        int cid = 0;
        conversion = new BatchConversion();
        conversion.setMessageDisplay(new SwingMessageDisplay(this));
        Object mode = ConversionMode.getById(cid);
        while (mode != null)
        {
            modeCombo.addItem(mode);
            mode = ConversionMode.getById(++cid);
        }
        
        SortedMap charsets = Charset.availableCharsets();
        // must use separate models, otherwise a change in one combo also
        // changes the other
        DefaultComboBoxModel iCharsetModel = 
           new DefaultComboBoxModel(charsets.keySet().toArray());
        DefaultComboBoxModel oCharsetModel = 
           new DefaultComboBoxModel(charsets.keySet().toArray());
        iEncCombo.setModel(iCharsetModel);
        oEncCombo.setModel(oCharsetModel);
        int utf8Index = iCharsetModel.getIndexOf("UTF-8");
        iEncCombo.setSelectedIndex(utf8Index);
        oEncCombo.setSelectedIndex(utf8Index);
        parseConverters();
        setOutputMode();
        
    }
    
    public void parseConverters()
    {
        ConverterXmlParser xmlParser = 
            new ConverterXmlParser(getConverterPath());
        if (!xmlParser.parse())
        {
            JOptionPane.showMessageDialog(this, 
                new JScrollPane(new JTextArea(xmlParser.getErrorLog())),
                "Error parsing Converter Configuration",
                JOptionPane.WARNING_MESSAGE);
        }
        availableConverters = xmlParser.getConverters();
        aModel = new DefaultListModel();
        sModel = new DefaultListModel();
        for (int i = 0; i<availableConverters.size(); i++)
        {
            aModel.addElement(availableConverters.get(i));
        }
        availableCList.setModel(aModel);
        selectedCList.setModel(sModel);
    }
    
    static File getConverterPath()
    {
        File converterConfigPath = Config.getCurrent().getConverterPath();
        System.out.println("Using config dir:" + 
            converterConfigPath.getAbsolutePath());
        return converterConfigPath;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    outputBGroup = new javax.swing.ButtonGroup();
    converterPanel = new javax.swing.JPanel();
    availPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    availableCList = new javax.swing.JList();
    middlePanel = new javax.swing.JPanel();
    fileModePanel = new javax.swing.JPanel();
    modeCombo = new javax.swing.JComboBox();
    jPanel10 = new javax.swing.JPanel();
    addConverter = new javax.swing.JButton();
    removeConverter = new javax.swing.JButton();
    selectedPanel = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane();
    selectedCList = new javax.swing.JList();
    encPanel = new javax.swing.JPanel();
    iEncPanel = new javax.swing.JPanel();
    iEncCombo = new javax.swing.JComboBox();
    oEncPanel = new javax.swing.JPanel();
    oEncCombo = new javax.swing.JComboBox();
    jPanel3 = new javax.swing.JPanel();
    inputPanel = new javax.swing.JPanel();
    iFileScroll = new javax.swing.JScrollPane();
    inputList = new javax.swing.JList();
    iFilePanel = new javax.swing.JPanel();
    iFileAdd = new javax.swing.JButton();
    iFileRemove = new javax.swing.JButton();
    loadFileList = new javax.swing.JButton();
    saveFileList = new javax.swing.JButton();
    outputPanel = new javax.swing.JPanel();
    jPanel5 = new javax.swing.JPanel();
    outputPrefix = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    oModeLabel = new javax.swing.JLabel();
    individualOutput = new javax.swing.JRadioButton();
    prefixOutput = new javax.swing.JRadioButton();
    oFileButton = new javax.swing.JButton();
    jPanel8 = new javax.swing.JPanel();
    convertButton = new javax.swing.JButton();
    testButton = new javax.swing.JButton();
    configButton = new javax.swing.JButton();
    exitButton = new javax.swing.JButton();
    progressPanel = new javax.swing.JPanel();
    progressLabel = new javax.swing.JLabel();
    jProgressBar = new javax.swing.JProgressBar();

    setTitle("Document Character Converter");
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        closeDialog(evt);
      }
    });

    converterPanel.setLayout(new javax.swing.BoxLayout(converterPanel, javax.swing.BoxLayout.X_AXIS));

    availPanel.setLayout(new java.awt.BorderLayout());

    availPanel.setBorder(new javax.swing.border.TitledBorder("Available Converters"));
    availableCList.setToolTipText("Select the converter that you want and click add.");
    jScrollPane1.setViewportView(availableCList);

    availPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    converterPanel.add(availPanel);

    middlePanel.setLayout(new javax.swing.BoxLayout(middlePanel, javax.swing.BoxLayout.Y_AXIS));

    fileModePanel.setBorder(new javax.swing.border.TitledBorder("File Mode"));
    modeCombo.setMaximumRowCount(5);
    modeCombo.setToolTipText("Choose the type of input files.");
    modeCombo.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        modeComboActionPerformed(evt);
      }
    });

    fileModePanel.add(modeCombo);

    middlePanel.add(fileModePanel);

    jPanel10.setLayout(new java.awt.GridLayout(2, 1));

    addConverter.setText(" Add");
    addConverter.setAlignmentX(0.5F);
    addConverter.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    addConverter.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        addConverterActionPerformed(evt);
      }
    });

    jPanel10.add(addConverter);

    removeConverter.setText("Remove");
    removeConverter.setAlignmentX(0.5F);
    removeConverter.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        removeConverterActionPerformed(evt);
      }
    });

    jPanel10.add(removeConverter);

    middlePanel.add(jPanel10);

    converterPanel.add(middlePanel);

    selectedPanel.setLayout(new java.awt.BorderLayout());

    selectedPanel.setBorder(new javax.swing.border.TitledBorder("Selected Converters"));
    selectedCList.setToolTipText("Select a converter and click remove to remove a converter.");
    jScrollPane2.setViewportView(selectedCList);

    selectedPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

    encPanel.setLayout(new java.awt.GridLayout(2, 1));

    iEncPanel.setBorder(new javax.swing.border.TitledBorder("Input Encoding"));
    iEncPanel.add(iEncCombo);

    encPanel.add(iEncPanel);

    oEncPanel.setBorder(new javax.swing.border.TitledBorder("Output Encoding"));
    oEncPanel.add(oEncCombo);

    encPanel.add(oEncPanel);

    selectedPanel.add(encPanel, java.awt.BorderLayout.SOUTH);

    converterPanel.add(selectedPanel);

    getContentPane().add(converterPanel, java.awt.BorderLayout.NORTH);

    jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

    inputPanel.setLayout(new javax.swing.BoxLayout(inputPanel, javax.swing.BoxLayout.Y_AXIS));

    inputPanel.setBorder(new javax.swing.border.TitledBorder("Input Files"));
    inputPanel.setMinimumSize(new java.awt.Dimension(50, 91));
    inputList.setToolTipText("File(s) to convert (original file will not be overwritten).");
    iFileScroll.setViewportView(inputList);

    inputPanel.add(iFileScroll);

    iFilePanel.setMinimumSize(new java.awt.Dimension(50, 35));
    iFileAdd.setText("Add file...");
    iFileAdd.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        iFileAddActionPerformed(evt);
      }
    });

    iFilePanel.add(iFileAdd);

    iFileRemove.setText("Remove");
    iFileRemove.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        iFileRemoveActionPerformed(evt);
      }
    });

    iFilePanel.add(iFileRemove);

    loadFileList.setText("Load list...");
    loadFileList.setToolTipText("Load a list of files from a text file. (2 entries per line: inputFile outputFile)");
    loadFileList.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        loadFileListActionPerformed(evt);
      }
    });

    iFilePanel.add(loadFileList);

    saveFileList.setText("Save List...");
    saveFileList.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        saveFileListActionPerformed(evt);
      }
    });

    iFilePanel.add(saveFileList);

    inputPanel.add(iFilePanel);

    jPanel3.add(inputPanel);

    outputPanel.setLayout(new javax.swing.BoxLayout(outputPanel, javax.swing.BoxLayout.Y_AXIS));

    outputPanel.setBorder(new javax.swing.border.TitledBorder("Output Prefix"));
    outputPanel.setMinimumSize(new java.awt.Dimension(50, 81));
    jPanel5.setLayout(new javax.swing.BoxLayout(jPanel5, javax.swing.BoxLayout.Y_AXIS));

    jPanel5.setMinimumSize(new java.awt.Dimension(50, 29));
    outputPrefix.setToolTipText("The converted files will be saved with this Prefix (this is usually a directory)");
    outputPrefix.addFocusListener(new java.awt.event.FocusAdapter()
    {
      public void focusLost(java.awt.event.FocusEvent evt)
      {
        outputPrefixFocusLost(evt);
      }
    });

    jPanel5.add(outputPrefix);

    outputPanel.add(jPanel5);

    jPanel1.setMinimumSize(new java.awt.Dimension(50, 35));
    oModeLabel.setText("Output mode:");
    jPanel1.add(oModeLabel);

    outputBGroup.add(individualOutput);
    individualOutput.setSelected(true);
    individualOutput.setText("Specify individually");
    individualOutput.setToolTipText("Specify a separate output file for each input file.");
    individualOutput.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        individualOutputActionPerformed(evt);
      }
    });

    jPanel1.add(individualOutput);

    outputBGroup.add(prefixOutput);
    prefixOutput.setText("Use output prefix");
    prefixOutput.setToolTipText("Converted files names will be created by adding the original filename to the Output Prefix.");
    prefixOutput.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        prefixOutputActionPerformed(evt);
      }
    });

    jPanel1.add(prefixOutput);

    oFileButton.setText("Browse...");
    oFileButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        oFileButtonActionPerformed(evt);
      }
    });

    jPanel1.add(oFileButton);

    outputPanel.add(jPanel1);

    jPanel3.add(outputPanel);

    getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

    jPanel8.setLayout(new java.awt.GridLayout(0, 1));

    jPanel8.setBorder(new javax.swing.border.TitledBorder("Actions"));
    convertButton.setText("Convert");
    convertButton.setToolTipText("Start conversion");
    convertButton.setAlignmentX(0.5F);
    convertButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    convertButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        convertButtonActionPerformed(evt);
      }
    });

    jPanel8.add(convertButton);

    testButton.setText("Test");
    testButton.setToolTipText("Testout the selected converter with your own string");
    testButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    testButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        testButtonActionPerformed(evt);
      }
    });

    jPanel8.add(testButton);

    configButton.setText("Config");
    configButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    configButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        configButtonActionPerformed(evt);
      }
    });

    jPanel8.add(configButton);

    exitButton.setText("Exit");
    exitButton.setToolTipText("Exit Character Converter");
    exitButton.setAlignmentX(0.5F);
    exitButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    exitButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        exitButtonActionPerformed(evt);
      }
    });

    jPanel8.add(exitButton);

    getContentPane().add(jPanel8, java.awt.BorderLayout.EAST);

    progressPanel.setLayout(new java.awt.GridLayout(2, 2));

    progressPanel.setBorder(new javax.swing.border.TitledBorder("Progress"));
    progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    progressLabel.setText("1. Select Converter 2. Select Input Files 3. Select Output Prefix 4. Click Convert");
    progressPanel.add(progressLabel);

    progressPanel.add(jProgressBar);

    getContentPane().add(progressPanel, java.awt.BorderLayout.SOUTH);

    pack();
  }
  // </editor-fold>//GEN-END:initComponents

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_testButtonActionPerformed
    {//GEN-HEADEREND:event_testButtonActionPerformed
        if (sModel.size() > 0)
        {
            if (sModel.getElementAt(0) instanceof ChildConverter)
            {
                Charset inCharset = Charset.forName(iEncCombo.getSelectedItem()
                                                    .toString());
                Charset outCharset = Charset.forName(oEncCombo.getSelectedItem()
                                                     .toString());
                
                ChildConverter cc = (ChildConverter)sModel.getElementAt(0);
                ChildConverter rcc = getReverseConverter(cc);
                cc.setEncodings(inCharset, outCharset);
                rcc.setEncodings(outCharset, inCharset);
                new TestDialog(this, true, cc, rcc).setVisible(true);
            }
            else System.out.println("unexpected converter type");
        }
        else
        {
            
            JOptionPane.showMessageDialog(this,
                msgResource.getString("selectConv"),
                msgResource.getString("noConvSelected"),
                JOptionPane.INFORMATION_MESSAGE);
            
        }
    }//GEN-LAST:event_testButtonActionPerformed

    protected ChildConverter getReverseConverter(ChildConverter cc)
    {
        ChildConverter rcc = null;
        if (cc.getParent() instanceof ReversibleConverter)
        {
            // try to find the reverse converter
            ReversibleConverter ccParent = 
                (ReversibleConverter)cc.getParent();
            int i = 0;
            while (rcc == null && i<availableConverters.size())
            {
                if (availableConverters.elementAt(i) != null)
                {
                    Object rco = availableConverters.elementAt(i);
                    if (rco instanceof CharConverter)
                    {
                        ChildConverter tempCc = null;
                        tempCc = (ChildConverter)rco;
                        if (tempCc.getParent() instanceof ReversibleConverter)
                        {
                            ReversibleConverter tempParent = 
                                (ReversibleConverter)tempCc.getParent();
                            if (tempParent.getBaseName().equals(ccParent.getBaseName()) 
                               && (tempParent.isForwards() != 
                                   ccParent.isForwards()))
                            {
                                rcc = tempCc;
                                break;
                            }
                        }
                    }
                }
                i++;
            }
        }
        return rcc;
    }
    
    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	boolean started = false;
    	final String [] browsers = {
    		"firefox",
    		"C:\\Program Files\\Mozilla Firefox\\firefox.exe",
    		"explorer.exe"
    	};
    	File docDir = new File(Config.getCurrent().getConverterPath().getParentFile(), DOCS_DIR);
    	if (!docDir.isDirectory())
    	{
    		docDir = new File(Config.DEFAULT_WIN_INSTALL);
    	}
    	File htmlFile = new File(docDir, "index.html");
    	if (docDir.isDirectory() && htmlFile.exists())
    	{
	    	for (int b = 0; b < browsers.length; b++)
	    	{
	    		ProcessBuilder pb = new ProcessBuilder(browsers[b], 
	    	        htmlFile.getAbsolutePath());
	    		try
	    		{
	    			pb.start();
	    			started = true;
	    			break;
	    		}
	    		catch (java.io.IOException e)
	    		{
	    			System.out.println(e);
	    		}
	    	}
    	}
    	if (!started)
    	{
    		JOptionPane.showMessageDialog(this, msgResource.getString("noDocs"));
    	}
    }
    
    private void saveFileListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveFileListActionPerformed
    {//GEN-HEADEREND:event_saveFileListActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file)
            {
                if (file.isDirectory()) return true;
                if (file.getName().toLowerCase().endsWith(".txt")) 
                    return true;
                return false;
            }
            public String getDescription() { return "Text files"; }
        });
        chooser.setCurrentDirectory(Config.getCurrent().getInputPath());
        if (chooser.showSaveDialog(this)  == 
            JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File listFile = chooser.getSelectedFile();
                Config.getCurrent().setInputPath(listFile.getParentFile());
                ConversionHelper.saveFileList(conversion, listFile);
            }
            catch (java.io.IOException e)
            {
                System.out.println(e.getLocalizedMessage());
                JOptionPane.showMessageDialog(null, e.getLocalizedMessage(),
                Config.getCurrent().getMsgResource().getString("saveListError"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_saveFileListActionPerformed

    private void modeComboActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_modeComboActionPerformed
    {//GEN-HEADEREND:event_modeComboActionPerformed
        if (modeCombo.getSelectedItem() != null)
        {
            conversion.setConversionMode((ConversionMode)modeCombo.getSelectedItem());
            if (conversion.getConversionMode().hasStyleSupport() == false)
            {
                while (sModel != null && sModel.size()>1)
                {
                    Object c = sModel.remove(0);
                    aModel.addElement(c);
                    conversion.removeConverter((CharConverter)c);
                }
            }
        }
    }//GEN-LAST:event_modeComboActionPerformed

    private void configButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_configButtonActionPerformed
    {//GEN-HEADEREND:event_configButtonActionPerformed
        (new ConfigDialog(this,true)).setVisible(true);
        parseConverters();
    }//GEN-LAST:event_configButtonActionPerformed

    
    private void loadFileListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadFileListActionPerformed
    {//GEN-HEADEREND:event_loadFileListActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file)
            {
                if (file.isDirectory()) return true;
                if (file.getName().toLowerCase().endsWith(".txt")) 
                    return true;
                return false;
            }
            public String getDescription() { return "Text files"; }
        });
        chooser.setCurrentDirectory(Config.getCurrent().getInputPath());
        if (chooser.showDialog(this, "Open File List")  == 
            JFileChooser.APPROVE_OPTION)
        {
            
            File listFile = chooser.getSelectedFile();
            Config.getCurrent().setInputPath(listFile.getParentFile());
            prefixOutput.setSelected(false);
            individualOutput.setSelected(true);
            setOutputMode();
            MainForm.loadFileList(conversion, listFile); 
            if (conversion.getInputFileList() != null)
              inputList.setListData(conversion.getInputFileList());              
        }
    }//GEN-LAST:event_loadFileListActionPerformed

    protected static void loadFileList(BatchConversion conv, File fileList)
    {
        ConversionHelper.setMsgDisplay(new SwingMessageDisplay(null));
        ConversionHelper.loadFileList(conv, fileList);
    }
    
    private void prefixOutputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_prefixOutputActionPerformed
    {//GEN-HEADEREND:event_prefixOutputActionPerformed
        setOutputMode();
    }//GEN-LAST:event_prefixOutputActionPerformed

    private void individualOutputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_individualOutputActionPerformed
    {//GEN-HEADEREND:event_individualOutputActionPerformed
        setOutputMode();
    }//GEN-LAST:event_individualOutputActionPerformed

    private void setOutputMode()
    {
        if (individualOutput.isSelected())
        {
            conversion.setPairsMode(true);
            outputPrefix.setEnabled(false);
            saveFileList.setEnabled(true);
        }
        else
        {
            conversion.setPairsMode(false);
            outputPrefix.setEnabled(true);
            saveFileList.setEnabled(false);
        }
    }
    
    private void outputPrefixFocusLost(java.awt.event.FocusEvent evt)//GEN-FIRST:event_outputPrefixFocusLost
    {//GEN-HEADEREND:event_outputPrefixFocusLost
        conversion.setOutputPrefix(outputPrefix.getText());
    }//GEN-LAST:event_outputPrefixFocusLost

    private void removeConverterActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeConverterActionPerformed
    {//GEN-HEADEREND:event_removeConverterActionPerformed
        int index = selectedCList.getSelectedIndex();
        if (index > -1)
        {
            DefaultListModel aModel = 
                (DefaultListModel)availableCList.getModel();
            DefaultListModel sModel = 
                (DefaultListModel)selectedCList.getModel();
            Object converter = sModel.remove(index);
            aModel.addElement(converter);
            conversion.removeConverter((CharConverter)converter);
        }
    }//GEN-LAST:event_removeConverterActionPerformed

    private void addConverterActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addConverterActionPerformed
    {//GEN-HEADEREND:event_addConverterActionPerformed
        int index = availableCList.getSelectedIndex();
        if (index > -1)
        {
            DefaultListModel aModel = 
                (DefaultListModel)availableCList.getModel();
            DefaultListModel sModel = 
                (DefaultListModel)selectedCList.getModel();
            if (conversion.getConversionMode().hasStyleSupport() == false &&
                sModel.size() > 0)
            {
                Object oldConverter = sModel.remove(0);
                if (oldConverter != null)
                {
                    aModel.addElement(oldConverter);
                }
            }
            Object converter = aModel.remove(index);
            sModel.addElement(converter);
            conversion.addConverter((CharConverter)converter);
        }
    }//GEN-LAST:event_addConverterActionPerformed

    private void iFileRemoveActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_iFileRemoveActionPerformed
    {//GEN-HEADEREND:event_iFileRemoveActionPerformed
        // Add your handling code here:
        Object [] files = inputList.getSelectedValues();
        for (int j = 0; j<files.length; j++)
        {
            if (individualOutput.isSelected())
            {
                assert (files[j] instanceof Map.Entry);
                conversion.removeFilePair((Map.Entry)files[j]);
            }
            else
            {
                assert (files[j] instanceof File);
                conversion.removeInputFile((File)files[j]);
            }
        }
        inputList.setListData(conversion.getInputFileList());
    }//GEN-LAST:event_iFileRemoveActionPerformed

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        // Add your handling code here:
        close();
        
    }//GEN-LAST:event_exitButtonActionPerformed

    private void convertButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_convertButtonActionPerformed
    {//GEN-HEADEREND:event_convertButtonActionPerformed
        // Add your handling code here:
        if (!conversion.isValid())
        {
            JOptionPane.showMessageDialog(this, 
                "You must select a converter and at least one input file and output location.",
                "Insufficient Options",JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        conversion.setInputEncoding(Charset.forName(iEncCombo.getSelectedItem().toString()));
        conversion.setOutputEncoding(Charset.forName(oEncCombo.getSelectedItem().toString()));
        
        jProgressBar.setIndeterminate(true);
        disableButtons();
        new Thread(conversion).start();
        progressLabel.setText("Please wait. Starting conversion...");
        if (timer == null)
        {            
            ActionListener taskPerformer = new ActionListener() {
              public void actionPerformed(ActionEvent evt) {
                  if (conversion.isRunning())
                  {
                      progressLabel.setText("Please wait. Processing: " + 
                          conversion.getFileBeingConverted() + " (" +
                          conversion.getCurrentFileIndex() + "/" +
                          conversion.getFileCount() + ") " + 
                          conversion.getProgressDesc());
                      if (conversion.getCurrentFileIndex() > 0)
                      {
                          jProgressBar.setIndeterminate(false);
                          jProgressBar.setMinimum(0);
                          jProgressBar.setMaximum(conversion.getFileCount());
                          jProgressBar.setValue(conversion.getCurrentFileIndex() - 1);
                      }
                  }
                  else
                  {
                      progressLabel.setText("Conversion finished.");
                      jProgressBar.setValue(conversion.getFileCount());
                      jProgressBar.setIndeterminate(false);
                      enableButtons();
                      timer.stop();
                  }
              }
            };
            timer = new Timer(TIMER_DELAY, taskPerformer);            
        }
        timer.start();
    }//GEN-LAST:event_convertButtonActionPerformed

    private void oFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_oFileButtonActionPerformed
    {//GEN-HEADEREND:event_oFileButtonActionPerformed
        
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(false);
        chooser.setCurrentDirectory(Config.getCurrent().getOutputPath());
        //chooser.setFileFilter(conversion.getConversionMode().getFileFilter());
        chooser.setFileFilter(ConversionMode.getAllFilesFilter());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showDialog(this, "Set Output Folder")  == 
            JFileChooser.APPROVE_OPTION)
        {
            File chosen = chooser.getSelectedFile();
            Config.getCurrent().setOutputPath(chosen.getParentFile());
            conversion.setOutputPrefix(chosen.getAbsolutePath());
            outputPrefix.setText(chosen.getAbsolutePath());
        }
    }//GEN-LAST:event_oFileButtonActionPerformed

    private void iFileAddActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_iFileAddActionPerformed
    {//GEN-HEADEREND:event_iFileAddActionPerformed
        // Add your handling code here:
        JFileChooser chooser = new JFileChooser();
        if (prefixOutput.isSelected())
        {
            chooser.setMultiSelectionEnabled(true);
        }
        chooser.setFileFilter(ConversionMode.getAllFilesFilter());
        chooser.setCurrentDirectory(Config.getCurrent().getInputPath());
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showDialog(this, 
            msgResource.getString("addFile"))  == 
            JFileChooser.APPROVE_OPTION)
        {
            Config.getCurrent().setInputPath
                (chooser.getSelectedFile().getParentFile());
            if (prefixOutput.isSelected())
            {
                conversion.addInputFiles(chooser.getSelectedFiles()); 
                inputList.setListData(conversion.getInputFileList());
            }
            else
            {
                File iFile = chooser.getSelectedFile();
                //MessageFormat mf = new MessageFormat("");
                Object [] args = {iFile.getName()};
                String msg = MessageFormat.format(msgResource.getString("setOutputFileTitle"), 
                                     args);
                chooser.setDialogTitle(msg);
                chooser.setCurrentDirectory
                    (Config.getCurrent().getOutputPath());
                if (chooser.showDialog(this, 
                    msgResource.getString("outputFile"))
                      == JFileChooser.APPROVE_OPTION)
                {
                    File oFile = chooser.getSelectedFile();
                    Config.getCurrent().setOutputPath(oFile);
                    conversion.addFilePair(iFile, oFile);
                    inputList.setListData(conversion.getInputFileList()); 
                }
            }
        }
        
    }//GEN-LAST:event_iFileAddActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
    {
        close();
    }//GEN-LAST:event_closeDialog
    
    private void close()
    {
        boolean doShutdown = true;
        if (conversion.isRunning())
        {
            if (JOptionPane.showConfirmDialog(this,
                    msgResource.getString("confirmAbortConversion"),
                    msgResource.getString("conversionInProgress"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                doShutdown = false; 
            }
            else
            {
                conversion.stopConversion();    
                doShutdown = false; // do we want to exit here or just stop?
            }
        }
        if (doShutdown)
        {
            conversion.destroy();
            Config.getCurrent().save();
            setVisible(false);
            dispose();
            System.exit(0);
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        if (args.length > 0)
        {            
            System.exit(runCommandLine(args));
        }
        
        final JFrame frame = new JFrame();
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = null;
        final MainForm mainForm = new MainForm();
        if (Config.getCurrent().getMsgResource() != null)
        {
          frame.setTitle(Config.getCurrent().getMsgResource().getString("dialogTitle"));
          frame.setIconImage(new javax.swing.ImageIcon(frame.getClass().
              getResource("/" + mainForm.resourceBase + 
              "/icons/DocCharConvert16.png")).getImage());
          label = new JLabel(Config.getCurrent().getMsgResource().getString("init_wait"));
          panel.add(label,BorderLayout.CENTER);
          frame.getContentPane().add(panel);
        }
        frame.pack();
        frame.setVisible(true);
         
        
        if (Config.getCurrent().getMsgResource() != null)
        {
          mainForm.setTitle(Config.getCurrent().getMsgResource().getString("dialogTitle"));
          mainForm.setIconImage(new javax.swing.ImageIcon(frame.getClass().
              getResource("/" + mainForm.resourceBase + 
              "/icons/DocCharConvert16.png")).getImage());
        }
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //frame.setEnabled(false);
        if (label != null)
          label.setText(Config.getCurrent().getMsgResource().getString("init_finished"));
        //mainForm.show();
        frame.setVisible(false);
        mainForm.setVisible(true);
    }
    
    private static int runCommandLine(String args[])
    {
        if (args.length > 0)
        {
            final int NORMAL = 0;
            final int IN_ENC = 1;
            final int OUT_ENC = 2;
            final int FILE_LIST = 3;
            final int OOO_SETUP = 4;
            Charset inEnc = null;
            Charset outEnc = null;
            int normalArgCount = 0;
            int state = NORMAL;
            String converter = null;
            String mode = null;
            String inputPath = null;
            String outputPath = null;
            File fileList = null;
            boolean isReverse = false;
            for (int a = 0; a<args.length; a++)
            {
              if (state == NORMAL)
              {
                if (args[a].equals("-i")) 
                {
                  state = IN_ENC;
                  continue;
                }
                else if (args[a].equals("-o")) 
                {
                  state = OUT_ENC;
                  continue;
                }
                else if (args[a].equals("-f")) 
                {
                  state = FILE_LIST;
                  continue;
                }
                else if (args[a].equals("-r")) 
                {
                  state = NORMAL;
                  isReverse = true;
                  continue;
                }
                else if (args[a].equals("--help")) 
                {
                  printUsage();
                  return 0;
                }
                else if (args[a].equals("--oopath")) 
                {
                  state = OOO_SETUP;
                  continue;
                }
                else if (args[a].startsWith("-"))
                {
                  System.out.println("Unknown option: " + args[a]);
                  printUsage();
                  return 3;
                }
                else
                {
                  switch (normalArgCount)
                  {
                    case 0:
                      converter = args[a];
                      break;
                    case 1:
                      mode = args[a];
                      break;
                    case 2: 
                      inputPath = args[a];
                      break;
                    case 3:
                      outputPath = args[a];
                      break;
                    default:
                      System.out.println("Ignoring argument " + args[a]);
                  }
                  normalArgCount++;
                }
              }
              else 
              {
                try
                {
                  switch (state)
                  {
                  case IN_ENC:
                    inEnc = Charset.forName(args[a]);
                    break;
                  case OUT_ENC:
                    outEnc = Charset.forName(args[a]);
                    break;
                  case FILE_LIST:
                    fileList = new File(args[a]);
                    break;
                  case OOO_SETUP:
                    Config.getCurrent().setOOPath(args[a]);
                    return 0;
                  }
                }
                catch (java.nio.charset.IllegalCharsetNameException e)
                {
                  System.out.println("Illegal Charset: " + e.getLocalizedMessage());
                  printUsage();
                  return 5;
                }
                catch (java.nio.charset.UnsupportedCharsetException e)
                {
                  System.out.println("Unknown charset: "+ e.getLocalizedMessage());
                  printUsage();
                  return 6;
                }
                state = NORMAL;
              }
              
            }
            // check that minimum number of arguments reached
            if (normalArgCount < 4)
            {
              if (normalArgCount < 2 || fileList == null)
              {
                printUsage();
                return 7;
              }
            }
            BatchConversion conv = new BatchConversion();
            try 
            {
              conv.setConversionMode(ConversionMode.getById(Integer.parseInt(mode)));
              conv.setCommandLine(true);
            }
            catch (NumberFormatException e)
            {
              System.out.println(e.getLocalizedMessage());
              printUsage();
              return 4;
            }
            if (inEnc != null) conv.setInputEncoding(inEnc);
            else conv.setInputEncoding(Charset.forName("UTF-8"));
            if (outEnc != null) conv.setOutputEncoding(outEnc);
            else conv.setOutputEncoding(Charset.forName("UTF-8"));
            
            File convPath = getConverterPath();
            ConverterXmlParser xmlParser = 
                new ConverterXmlParser(convPath);
            File convXml = new File(convPath, converter);
            if (!xmlParser.parseFile(convXml))
            {
                System.out.println(xmlParser.getErrorLog());
                return 2;
            }
            int convIndex = 0; 
            while (convIndex < xmlParser.getConverters().size())
            {
              CharConverter cc =
                (CharConverter)xmlParser.getConverters().elementAt(convIndex++);
              if (cc instanceof ChildConverter)
              {
                ChildConverter cccc = (ChildConverter)cc;
                if (cccc.getParent() instanceof ReversibleConverter)
                {
                  if (((ReversibleConverter)cccc.getParent()).isForwards())
                  {
                    if (!isReverse) 
                    {
                        conv.addConverter(cc);
                        System.out.println(cc);  
                    }
                  }
                  else
                  {
                    if (isReverse) 
                    {
                        conv.addConverter(cc);     
                        System.out.println(cc);  
                    } 
                  }
                }
                else conv.addConverter(cc);
              }
              else conv.addConverter(cc);
            }
            conv.setPairsMode(true);
            if (fileList == null)
            {
              File input = new File(inputPath);
              File output = new File(outputPath);
              conv.addFilePair(input,output);
            }
            else
            {
                MainForm.loadFileList(conv, fileList);
            }
            new Thread(conv).start();
            do 
            {
                try { Thread.sleep(100); }
                catch (java.lang.InterruptedException e) {}
            } while (conv.isRunning());
            conv.destroy();
            return 0;
        }
        else
        {
            printUsage();
            return 1;
        }
    }
    
    private static void printUsage()
    {
      File [] converterFiles = ConverterXmlParser.getConverterFiles(getConverterPath());
      System.out.println("Arguments: [-i iEnc] [-o oEnc] [-r] converter.dccx mode "); 
      System.out.println("           [-f list]|[inputFile outputFile]");
      System.out.println("Modes:");
      for (int m = 0; m<ConversionMode.NUM_MODES; m++)
      {
          System.out.println("\t" + m + "\t" + ConversionMode.getById(m));
      }
      System.out.println("Optional Arguments:");
      System.out.println("\t--help display this help");
      System.out.println("\t-r use the converter in reverse mode");
      System.out.println("\t-i iEnc = input encoding e.g. -i iso-8859-1 (default UTF-8)");
      System.out.println("\t-o oEnc = output encoding e.g. -o iso-8859-1 (default UTF-8)");
      System.out.println("\t-f fileList = file containing list input output files");
      System.out.println("Please choose from one of the following converters:");
      for (int i = 0; i<converterFiles.length; i++)
      {
          System.out.println("\t" + converterFiles[i].getName());          
      }
      System.out.println("Run with no arguments for Graphical mode and Configuration editor.");
    }
    
    private void enableButtons()
    {
        addConverter.setEnabled(true);
        removeConverter.setEnabled(true);
        convertButton.setEnabled(true);
        configButton.setEnabled(true);
        iFileAdd.setEnabled(true);
        iFileRemove.setEnabled(true);
        oFileButton.setEnabled(true);
        if (prefixOutput.isSelected()) 
        {
            outputPrefix.setEnabled(true);
        }
        modeCombo.setEnabled(true);
        availableCList.setEnabled(true);
        selectedCList.setEnabled(true);
        inputList.setEnabled(true);
        individualOutput.setEnabled(true);
        prefixOutput.setEnabled(true);
        loadFileList.setEnabled(true);
        oEncCombo.setEnabled(true);
        iEncCombo.setEnabled(true);
        outputPanel.setEnabled(true);
    }
    private void disableButtons()
    {
        addConverter.setEnabled(false);
        removeConverter.setEnabled(false);
        convertButton.setEnabled(false);
        configButton.setEnabled(false);
        iFileAdd.setEnabled(false);
        iFileRemove.setEnabled(false);
        oFileButton.setEnabled(false);
        outputPrefix.setEnabled(false);
        modeCombo.setEnabled(false);
        availableCList.setEnabled(false);
        selectedCList.setEnabled(false);
        inputList.setEnabled(false);
        individualOutput.setEnabled(false);
        prefixOutput.setEnabled(false);
        loadFileList.setEnabled(false);
        oEncCombo.setEnabled(false);
        iEncCombo.setEnabled(false);
        outputPanel.setEnabled(false);
    }
    public ResourceBundle getResource() { return guiResource; }
    
    
  private javax.swing.JButton helpButton;
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton addConverter;
  private javax.swing.JPanel availPanel;
  private javax.swing.JList availableCList;
  private javax.swing.JButton configButton;
  private javax.swing.JButton convertButton;
  private javax.swing.JPanel converterPanel;
  private javax.swing.JPanel encPanel;
  private javax.swing.JButton exitButton;
  private javax.swing.JPanel fileModePanel;
  private javax.swing.JComboBox iEncCombo;
  private javax.swing.JPanel iEncPanel;
  private javax.swing.JButton iFileAdd;
  private javax.swing.JPanel iFilePanel;
  private javax.swing.JButton iFileRemove;
  private javax.swing.JScrollPane iFileScroll;
  private javax.swing.JRadioButton individualOutput;
  private javax.swing.JList inputList;
  private javax.swing.JPanel inputPanel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JProgressBar jProgressBar;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JButton loadFileList;
  private javax.swing.JPanel middlePanel;
  private javax.swing.JComboBox modeCombo;
  private javax.swing.JComboBox oEncCombo;
  private javax.swing.JPanel oEncPanel;
  private javax.swing.JButton oFileButton;
  private javax.swing.JLabel oModeLabel;
  private javax.swing.ButtonGroup outputBGroup;
  private javax.swing.JPanel outputPanel;
  private javax.swing.JTextField outputPrefix;
  private javax.swing.JRadioButton prefixOutput;
  private javax.swing.JLabel progressLabel;
  private javax.swing.JPanel progressPanel;
  private javax.swing.JButton removeConverter;
  private javax.swing.JButton saveFileList;
  private javax.swing.JList selectedCList;
  private javax.swing.JPanel selectedPanel;
  private javax.swing.JButton testButton;
  // End of variables declaration//GEN-END:variables
  
}
