/*
 * MainForm.java
 *
 * Created on August 20, 2004, 2:23 PM
 */

package DocCharConvert;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.BorderLayout;
import java.awt.Dimension;
//import java.net.URL;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import DocCharConvert.Converter.CharConverter;
/**
 *
 * @author  keith
 */
public class MainForm extends javax.swing.JDialog
{
    private BatchConversion conversion = null;
    private Vector availableConverters = null;
    private DefaultListModel aModel = null;
    private DefaultListModel sModel = null;
    private Timer timer = null;
    private int TIMER_DELAY = 250;
    private java.awt.Frame parentFrame;
    /** Creates new form MainForm */
    public MainForm(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        this.parentFrame = parent;
        initComponents();
        int cid = 0;
        conversion = new BatchConversion(this);
        Object mode = ConversionMode.getById(cid);
        while (mode != null)
        {
            modeCombo.addItem(mode);
            mode = ConversionMode.getById(++cid);
        }
        
        
        parseConverters();
        
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
        File converterConfigPath = Config.getCurrent().getBasePath();
        System.out.println("Using config dir:" + 
            converterConfigPath.getAbsolutePath());
        return converterConfigPath;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()//GEN-BEGIN:initComponents
    {
        outputBGroup = new javax.swing.ButtonGroup();
        converterPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        availableCList = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        modeCombo = new javax.swing.JComboBox();
        jPanel10 = new javax.swing.JPanel();
        addConverter = new javax.swing.JButton();
        removeConverter = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedCList = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        inputPanel = new javax.swing.JPanel();
        iFileScroll = new javax.swing.JScrollPane();
        inputList = new javax.swing.JList();
        iFilePanel = new javax.swing.JPanel();
        iFileAdd = new javax.swing.JButton();
        iFileRemove = new javax.swing.JButton();
        loadFileList = new javax.swing.JButton();
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
        configButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
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

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel4.setBorder(new javax.swing.border.TitledBorder("Available Converters"));
        availableCList.setToolTipText("Select the converter that you want and click add.");
        jScrollPane1.setViewportView(availableCList);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        converterPanel.add(jPanel4);

        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.Y_AXIS));

        jPanel7.setBorder(new javax.swing.border.TitledBorder("File Mode"));
        modeCombo.setMaximumRowCount(5);
        modeCombo.setToolTipText("Choose the type of input files.");
        modeCombo.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                modeComboActionPerformed(evt);
            }
        });

        jPanel7.add(modeCombo);

        jPanel6.add(jPanel7);

        jPanel10.setLayout(new java.awt.GridLayout(2, 1));

        addConverter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/DocCharConvert/icons/Add.png")));
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

        removeConverter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/DocCharConvert/icons/Remove.png")));
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

        jPanel6.add(jPanel10);

        converterPanel.add(jPanel6);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(new javax.swing.border.TitledBorder("Selected Converters"));
        selectedCList.setToolTipText("Select a converter and click remove to remove a converter.");
        jScrollPane2.setViewportView(selectedCList);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        converterPanel.add(jPanel2);

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
        convertButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/DocCharConvert/icons/Convert.png")));
        convertButton.setText("Convert");
        convertButton.setToolTipText("Start conversion");
        convertButton.setAlignmentX(0.5F);
        convertButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                convertButtonActionPerformed(evt);
            }
        });

        jPanel8.add(convertButton);

        configButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/DocCharConvert/icons/Config.png")));
        configButton.setText("Config");
        configButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                configButtonActionPerformed(evt);
            }
        });

        jPanel8.add(configButton);

        exitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/DocCharConvert/icons/Exit.png")));
        exitButton.setText("Exit");
        exitButton.setToolTipText("Exit Character Converter");
        exitButton.setAlignmentX(0.5F);
        exitButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitButtonActionPerformed(evt);
            }
        });

        jPanel8.add(exitButton);

        getContentPane().add(jPanel8, java.awt.BorderLayout.EAST);

        jPanel9.setLayout(new java.awt.GridLayout(2, 2));

        jPanel9.setBorder(new javax.swing.border.TitledBorder("Progress"));
        progressLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        progressLabel.setText("1. Select Converter 2. Select Input Files 3. Select Output Prefix 4. Click Convert");
        jPanel9.add(progressLabel);

        jPanel9.add(jProgressBar);

        getContentPane().add(jPanel9, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents

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
        (new ConfigDialog(parentFrame,true)).setVisible(true);
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
            loadFileList(listFile);               
        }
    }//GEN-LAST:event_loadFileListActionPerformed

    protected void loadFileList(File fileList)
    {
        try
        {
            BufferedReader reader = new BufferedReader
                (new FileReader(fileList));
            Pattern p = Pattern.compile("([^\\s]*?[^\\\\])\\s+(.*)");
            StringBuffer invalidLines = new StringBuffer();
            String line = reader.readLine();
            // if we have got this far without an excpetion, 
            // then we assume file is OK
            prefixOutput.setSelected(false);
            individualOutput.setSelected(true);
            setOutputMode();

            while (line != null)
            {
                String trimmedLine = line.trim();
                Matcher m = p.matcher(trimmedLine);
                if (m.matches())
                {
                    File iFile = new File(m.group(1));
                    File oFile = new File(m.group(2));
                    // if iFile doesn't exist, try appending 
                    // listFile dir to it
                    if (!iFile.exists())
                    {
                        iFile = new File(fileList.getParent(),m.group(1));
                        oFile = new File(fileList.getParent(),m.group(2));
                    }
                    if (iFile.canRead() &&
                        oFile.getParentFile().exists())
                    {
                        conversion.addFilePair(iFile, oFile);
                    }
                    else
                    {
                        invalidLines.append(iFile.getAbsolutePath());
                        invalidLines.append(' ');
                        invalidLines.append(oFile.getAbsolutePath());
                        invalidLines.append(" (File not readable)\n");
                    }
                }
                else
                {
                    invalidLines.append(line);
                    invalidLines.append('\n');
                }
                line = reader.readLine();
            }
            if (invalidLines.length()>0)
            {
                Object [] msg = new Object[2];
                msg[0] = "Warning, the following lines were ignored";
                JScrollPane pane = 
                    new JScrollPane(new JTextArea(invalidLines.toString()));
                pane.setMaximumSize(new Dimension(300, 100));
                msg[1] = pane;                    
                JOptionPane.showMessageDialog(this, msg,
                    "Invalid format",
                    JOptionPane.WARNING_MESSAGE);
            }
            reader.close();
        }
        catch (java.io.IOException e)
        {
            JOptionPane.showMessageDialog(this,
                "Error reading " + fileList.getName() + "\n" + 
                e.getLocalizedMessage(),"Error loading list",
                JOptionPane.ERROR_MESSAGE);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // shouldn't happen in normal operation
            System.out.println(e.getMessage());
        }
        finally 
        {
            inputList.setListData(conversion.getInputFileList());
        }
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
        }
        else
        {
            conversion.setPairsMode(false);
            outputPrefix.setEnabled(false);
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
        if (conversion.isRunning())
        {
            JOptionPane.showMessageDialog(this,"Conversion is still running","Converting...",JOptionPane.WARNING_MESSAGE);
        }
        else
        {
            setVisible(false);
            dispose();
            System.exit(0);
        }
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
                          conversion.getFileCount() + ")");
                      if (conversion.getCurrentFileIndex() > 0)
                      {
                          jProgressBar.setIndeterminate(false);
                          jProgressBar.setMinimum(0);
                          jProgressBar.setMaximum(conversion.getFileCount());
                          jProgressBar.setValue(conversion.getCurrentFileIndex());
                      }
                  }
                  else
                  {
                      progressLabel.setText("Conversion finished.");
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
        if (chooser.showDialog(this, "Add File")  == 
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
                chooser.setDialogTitle("Choose Output file for " + 
                    iFile.getName());
                chooser.setCurrentDirectory
                    (Config.getCurrent().getOutputPath());
                if (chooser.showDialog(this, "Set Output File")  == 
                    JFileChooser.APPROVE_OPTION)
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
        conversion.destroy();
        Config.getCurrent().save();
        setVisible(false);
        dispose();
        System.exit(0);
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
        frame.setTitle("Document Character Converter ");
        frame.setIconImage(new javax.swing.ImageIcon(frame.getClass().
            getResource("/DocCharConvert/icons/DocCharConvert16.png"))
            .getImage());
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Please wait, initalising...");
        panel.add(label,BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
         
        
        final MainForm mainForm = new MainForm(frame, false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //frame.setEnabled(false);
        label.setText("Initialisation finished.");
        //mainForm.show();
        frame.setVisible(false);
        mainForm.setVisible(true);
    }
    
    private static int runCommandLine(String args[])
    {
        if (args.length == 4)
        {
            BatchConversion conv = new BatchConversion(null);
            conv.setConversionMode(ConversionMode.getById(Integer.parseInt(args[1])));
            
            File convPath = getConverterPath();
            ConverterXmlParser xmlParser = 
                new ConverterXmlParser(convPath);
            File convXml = new File(convPath, args[0]);
            if (!xmlParser.parseFile(convXml))
            {
                System.out.println(xmlParser.getErrorLog());
                return 2;
            }
            CharConverter cc = (CharConverter)xmlParser.getConverters().elementAt(0);
            conv.addConverter(cc);
            File input = new File(args[2]);
            File output = new File(args[3]);
            conv.setPairsMode(true);
            conv.addFilePair(input,output);
            conv.setPromptMode(BatchConversion.OVERWRITE_ALL);
            new Thread(conv).start();
            do 
            {
                try { Thread.sleep(100); }
                catch (java.lang.InterruptedException e) {}
            } while (conv.isRunning());
            System.out.println("Processed: " + input.getAbsolutePath() + " " +
                output.getAbsolutePath());
            conv.destroy();
            return 0;
        }
        else
        {
            System.out.println("Usage: converter.dccx mode inputFile outputFile");
            System.out.println("Modes:");
            for (int m = 0; m<ConversionMode.NUM_MODES; m++)
            {
                System.out.println("\t" + m + "\t" + ConversionMode.getById(m));
            }
        }
        return 1;
    }
    
    private void enableButtons()
    {
        addConverter.setEnabled(true);
        removeConverter.setEnabled(true);
        convertButton.setEnabled(true);
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
    }
    private void disableButtons()
    {
        addConverter.setEnabled(false);
        removeConverter.setEnabled(false);
        convertButton.setEnabled(false);
        iFileAdd.setEnabled(false);
        iFileRemove.setEnabled(false);
        oFileButton.setEnabled(false);
        outputPrefix.setEnabled(false);
        modeCombo.setEnabled(false);
        availableCList.setEnabled(false);
        selectedCList.setEnabled(false);
        inputList.setEnabled(false);
        loadFileList.setEnabled(false);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addConverter;
    private javax.swing.JList availableCList;
    private javax.swing.JButton configButton;
    private javax.swing.JButton convertButton;
    private javax.swing.JPanel converterPanel;
    private javax.swing.JButton exitButton;
    private javax.swing.JButton iFileAdd;
    private javax.swing.JPanel iFilePanel;
    private javax.swing.JButton iFileRemove;
    private javax.swing.JScrollPane iFileScroll;
    private javax.swing.JRadioButton individualOutput;
    private javax.swing.JList inputList;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton loadFileList;
    private javax.swing.JComboBox modeCombo;
    private javax.swing.JButton oFileButton;
    private javax.swing.JLabel oModeLabel;
    private javax.swing.ButtonGroup outputBGroup;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JTextField outputPrefix;
    private javax.swing.JRadioButton prefixOutput;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JButton removeConverter;
    private javax.swing.JList selectedCList;
    // End of variables declaration//GEN-END:variables
    
}
