/*
 * BatchConversion.java
 *
 * Created on August 20, 2004, 3:46 PM
 */

package DocCharConvert;

import java.awt.Component;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;
//import java.util.HashSet;
import java.io.File;
import java.util.Hashtable;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.lang.reflect.InvocationTargetException;

import DocCharConvert.Converter.CharConverter;
/**
 *
 * @author  keith
 */
public class BatchConversion implements Runnable
{
    
    Vector inputFileList;
    Hashtable converterList;
    String outputPrefix = "";
    File outputDir = null;
    ConversionMode mode = ConversionMode.OO_MODE;
    int currentFileIndex = 0;
    int fileCount = 0;
    boolean stop = false;
    boolean running = false;
    DocInterface docInterface = null;
    boolean useFilePairs = false;
    TreeMap filePairList = null;
    String status = "";
    public final static int PROMPT_YES = 0;
    public final static int PROMPT_NO = 1;
    public final static int OVERWRITE_ALL = 2;
    public final static int SKIP_ALL = 3;
    private final static String [] OPTIONS = {"Yes","No","Yes to all","No to all"};
    private int promptMode = PROMPT_NO;
    boolean onlyStylesInUse = true;
    boolean autoRetry = false;
    Component dialog = null;
    /** Creates a new instance of BatchConversion */
    public BatchConversion(Component dialog) 
    {
        this.dialog = dialog;
        inputFileList = new Vector();
        converterList = new Hashtable();
    }
    public void setConversionMode(ConversionMode mode)
    {
        this.mode = mode;
    }
    public void setPairsMode(boolean isPairs)
    {
        useFilePairs = isPairs;
        if (useFilePairs == true && filePairList == null)
            filePairList = new TreeMap();
    }
    public void setOnlyStylesInUse(boolean onlyConvertStylesInUse)
    {
        this.onlyStylesInUse = onlyConvertStylesInUse;
    }
    public void addFilePair(File oldF, File newF)
    {
        if (useFilePairs)
        {
            filePairList.put(oldF,newF);
        }
    }
    public void removeFilePair(Map.Entry entry)
    {
        removeFilePair((File)entry.getKey(),(File)entry.getValue());
    }
    public void removeFilePair(File oldF, File newF)
    {
        if (useFilePairs)
        {
            filePairList.remove(oldF);
        }
    }
    public ConversionMode getConversionMode()
    {
        return mode;
    }
    public void addInputFile(File f)
    {
        if (!inputFileList.contains(f))
        {
            inputFileList.add(f);
        }
    }
    public void addInputFiles(File [] fs)
    {
        for (int i = 0; i< fs.length; i++)
        {
            addInputFile(fs[i]);
        }
    }
    public void removeInputFile(File f)
    {
        if (!inputFileList.contains(f))
        {
            inputFileList.remove(f);
        }
    }
    public Object [] getInputFileList()
    {
        if (useFilePairs)
        {
            return filePairList.entrySet().toArray();
        }
        return inputFileList.toArray();
    }
    public void setOutputPrefix(String prefix)
    {
        outputPrefix = prefix;
        outputDir = new File(prefix);
        if (!outputDir.isDirectory())
        {
            outputDir = null;
        }
    }
    
    public File getOutputFile(File input)
    {
        File outputFile = null;
        if (useFilePairs)
        {
            outputFile = (File)filePairList.get(input);
        }
        else
        {
            if (outputDir != null)
            {
                outputFile = new File(outputDir, input.getName());
            }
            else if (outputPrefix.indexOf(File.separatorChar)>-1)
            {
                outputFile = new File(outputPrefix + input.getName());
                if (!outputFile.getParentFile().isDirectory())
                    outputFile = null;
            }
        }
        if (outputFile == null)
        {
            outputFile = new File(input.getParent() + File.separatorChar +
                outputPrefix + input.getName());
        }
        return outputFile;
    }
    public void addConverter(CharConverter cc)
    {
        if (!mode.hasStyleSupport())
        {
            // can only have one converter in text mode
            converterList.put("",cc);
        }
        else if (!converterList.containsValue(cc) &&
                 !converterList.containsKey(cc.getOldStyle()))
        {
            converterList.put(cc.getOldStyle(),cc);
        }
    }
    public void removeConverter(CharConverter cc)
    {
        if (!converterList.containsValue(cc))
        {
            converterList.remove(cc.getOldStyle());
        }
    }
    public boolean isValid()
    {
        boolean valid = true;
        if (converterList.size()==0) valid = false;        
        if (useFilePairs == true)
        {
            if (filePairList.size()==0) valid = false;
        }
        else
        {
            if (inputFileList.size()==0) valid = false;
            if (outputPrefix.length()==0) valid = false;
        }
        return valid;
    }
    synchronized public boolean isRunning() { return running; }
    
    public void run()
    {
        Iterator i;
        stop = false;
        synchronized (this)
        {
            running = true;
            status = "Initialising";        
            if (useFilePairs) 
            {
                fileCount = filePairList.size();
                i = filePairList.keySet().iterator();
            }
            else
            {
                fileCount = inputFileList.size();
                i = inputFileList.iterator();
            }
            currentFileIndex = 0;
        }
        //promptMode = PROMPT_NO;
        Iterator c = converterList.values().iterator();
        if (docInterface != null)
        {
            if (docInterface.getMode() != mode)
            {
                docInterface.destroy();
                docInterface = null;
            }
        }
        if (docInterface == null)
        {
            switch (mode.getId())
            {
                case ConversionMode.OO_ID:
                    OOMainInterface ooInterface = new OOMainInterface();
                    ooInterface.setOnlyStylesInUse(onlyStylesInUse);
                    docInterface = ooInterface;
                    break;
                case ConversionMode.TEXT_ID:
                    docInterface = new TextParser();
                    break;
                case ConversionMode.TEX_ID:
                    docInterface = new TeXParser();
                    break;
            }
        }
        if (docInterface == null) 
        {
            running = false; 
            System.out.println("No document interface!");
            showWarning("Failed to intialise document interface");
            return;
        }
        try 
        {
            docInterface.initialise();
            // initialise converters
            while (c.hasNext())
            {
                CharConverter cc = (CharConverter)c.next();
                cc.initialize();
            }
            
            boolean retry = false;
            File inputFile = null;
            while ((i.hasNext())&&(stop == false))
            {
                if (retry == false)
                {
                    inputFile = (File)i.next();
                    synchronized (this)
                    {
                        currentFileIndex++;
                        status = inputFile.getName();
                    }                
                }
                else
                {
                    docInterface.initialise();
                }
                File outputFile = getOutputFile(inputFile);
                final String filePath = outputFile.getAbsolutePath();
                if (outputFile.exists())
                {
                    if (promptMode == PROMPT_YES || promptMode == PROMPT_NO)
                    {
                        Runnable promptRunnable = new Runnable() {
                            public void run()
                            {
                                int option =
                                    JOptionPane.showOptionDialog(dialog, 
                                        "File " + filePath +
                                        " already exists. Do you want to overwrite?",
                                        "Overwrite?",
                                        JOptionPane.YES_NO_OPTION, 
                                        JOptionPane.WARNING_MESSAGE,null,
                                        OPTIONS,OPTIONS[PROMPT_NO]);
                                setPromptMode(option);                                            
                            }
                        };
                        try
                        {
                            SwingUtilities.invokeAndWait(promptRunnable);
                        }
                        catch (InterruptedException e) {}
                        catch (InvocationTargetException ite)
                        {
                            System.out.println(ite.getMessage());
                        }
                    }
                    switch (promptMode)
                    {
                        case PROMPT_YES:
                        case OVERWRITE_ALL:
                            break;
                        case PROMPT_NO:
                        case SKIP_ALL:
                            continue; // don't process this file
                    }
                }
                try
                {
                    docInterface.parse(inputFile,outputFile,converterList); 
                    retry = false;
                }
                catch (java.lang.Exception e) 
                {
                    e.printStackTrace();
                    // only retry if we have had at least one successful run
                    // since the last attempt
                    if (autoRetry && retry == false) 
                    {
                        retry = true;
                        docInterface.destroy();
                        continue;
                    }
                    retry = true;
                    final String errorMsg = e.getLocalizedMessage();
                    final String [] options = { OPTIONS[0],OPTIONS[1],OPTIONS[2]};
                    Runnable promptRunnable = new Runnable() {
                            public void run()
                            {
                                int option =
                                    JOptionPane.showOptionDialog(dialog, 
                                        "An error occured while converting "+
                                        filePath +
                                        ".\nDo you want to continue converting?\n"
                                        + errorMsg,
                                        "Error during conversion!",
                                        JOptionPane.YES_NO_OPTION, 
                                        JOptionPane.WARNING_MESSAGE,null,
                                        options,options[2]);
                                if (option == PROMPT_NO)
                                {
                                    stopConversion();
                                    // reset docInterface
                                    docInterface.destroy();
                                    docInterface = null;
                                }
                                else
                                {
                                    if (option == 2) autoRetry = true;
                                    docInterface.destroy();
                                }
                            }
                        };
                    try
                    {
                        SwingUtilities.invokeAndWait(promptRunnable);
                    }
                    catch (InterruptedException e2) {}
                    catch (InvocationTargetException ite2)
                    {
                        System.out.println(ite2.getMessage());
                    }
                }
            }
        
            c = converterList.values().iterator();
            // tidy up converters
            while (c.hasNext())
            {
                CharConverter cc = (CharConverter)c.next();
                cc.destroy();
            }
            status = "Finished";
            
        }
        catch (CharConverter.FatalException e)
        {
            System.out.println(e.getMessage());
            showWarning(e.getLocalizedMessage());
        }
        catch (DocInterface.InterfaceException e)
        {
            System.out.println(e.getMessage());
            showWarning(e.getLocalizedMessage());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            showWarning(e.getLocalizedMessage());
        }
        finally
        {
            running = false;
        }
    }
    protected void showWarning(final String warningMsg)
    {
        Runnable promptRunnable = new Runnable() {
            public void run()
            {
                JOptionPane.showMessageDialog(dialog,
                    "Conversion failed: (" + getFileBeingConverted() + 
                    ")\n" + warningMsg,
                    "Conversion Aborted!",JOptionPane.WARNING_MESSAGE);
            }
        };
        SwingUtilities.invokeLater(promptRunnable);
    }
    public synchronized int getCurrentFileIndex() { return currentFileIndex; }
    public synchronized int getFileCount() 
    {
        return fileCount;
    }
    public synchronized String getFileBeingConverted()
    {
        return status;
    }
    public synchronized void setPromptMode(int mode)
    {
        promptMode = mode;
    }
    public synchronized void stopConversion()
    {
        stop = true;
    }
    public void destroy()
    {
        stopConversion();
        int maxWait = 100;
        while (isRunning()&&(maxWait-->0))
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e) 
            {
                break;
            }
        }
        if (docInterface != null) 
        {
            docInterface.destroy();
            docInterface = null;
        }
    }
}
