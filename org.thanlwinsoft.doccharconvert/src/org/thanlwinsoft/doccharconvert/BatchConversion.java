/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/

package org.thanlwinsoft.doccharconvert;

//import java.net.URLClassLoader;
//import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;
//import java.util.HashSet;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Hashtable;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.eclipse.ExtensionConversionMode;
import org.thanlwinsoft.doccharconvert.opendoc.OpenDocParser;
import org.thanlwinsoft.doccharconvert.parser.TeXParser;
import org.thanlwinsoft.doccharconvert.parser.TextParser;
/**
 *
 * @author  keith
 */
public class BatchConversion implements Runnable
{
    
    Vector <File>inputFileList;
    Hashtable <TextStyle,CharConverter>converterList;
    String outputPrefix = "";
    File outputDir = null;
    ConversionMode mode = ConversionMode.TEXT_MODE;
    int currentFileIndex = 0;
    int fileCount = 0;
    //boolean stop = false;
    boolean running = false;
    DocInterface docInterface = null;
    boolean useFilePairs = false;
    TreeMap<File,File> filePairList = null;
    String status = "";
    
    IMessageDisplay.Option promptMode = IMessageDisplay.Option.NO;
    boolean onlyStylesInUse = true;
    boolean autoRetry = true; // because it is so common to loose the OO connection
    Charset iCharset = null;
    Charset oCharset = null;
    boolean commandLine = false;
    IMessageDisplay msgDisplay = new NoGuiMessageDisplay();
    boolean fileMode = false;
    ProgressNotifier notifier = new ProgressNotifier();
    
    /** Creates a new instance of BatchConversion */
    public BatchConversion() 
    {
        inputFileList = new Vector<File>();
        converterList = new Hashtable<TextStyle,CharConverter>();
    }
    /** Copy constructor for BatchConversion
     *  everything except the file list is copied
     *  @param bc
     */
    public BatchConversion(BatchConversion bc) 
    {
        inputFileList = new Vector<File>();
        converterList = bc.converterList;
        outputPrefix = bc.outputPrefix;
        outputDir = bc.outputDir;
        mode = bc.mode;
        docInterface = bc.docInterface;
        setPairsMode(bc.useFilePairs);
        promptMode = bc.promptMode;
        onlyStylesInUse = bc.onlyStylesInUse;
        autoRetry = bc.onlyStylesInUse;
        iCharset = bc.iCharset;
        oCharset = bc.oCharset;
        msgDisplay = bc.msgDisplay;
        fileMode = bc.fileMode;
        notifier = bc.notifier;
    }
    
    
    /**
     * @param cl
     */
    public void setCommandLine(boolean cl)
    {
      commandLine = cl;
    }
    /**
     * @param mode
     */
    public void setConversionMode(ConversionMode mode)
    {
        if (mode != this.mode)
        {
            if (docInterface != null)
                docInterface.destroy();
            docInterface = null;
            this.mode = mode;
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(MessageUtil.getString("BatchConversion",
        		getConversionMode().toString()));
        builder.append(" -");
        for(CharConverter cc : getConverters())
        {
            builder.append(' ');
            builder.append(cc.getName());
        }
        return builder.toString();
    }
    /**
     * 
     * @param isPairs
     */
    public void setPairsMode(boolean isPairs)
    {
        useFilePairs = isPairs;
        if (useFilePairs == true && filePairList == null)
            filePairList = new TreeMap<File,File>();
    }
    /**
     * 
     * @param onlyConvertStylesInUse
     */
    public void setOnlyStylesInUse(boolean onlyConvertStylesInUse)
    {
        this.onlyStylesInUse = onlyConvertStylesInUse;
    }
    /**
     * 
     * @param oldF
     * @param newF
     */
    public void addFilePair(File oldF, File newF)
    {
        if (useFilePairs)
        {   
            filePairList.put(oldF,newF);
        }
        else throw new IllegalArgumentException("Pairs mode is not enabled");
    }
    /**
     * 
     * @param entry
     */
    public void removeFilePair(Map.Entry<?,?> entry)
    {
        removeFilePair((File)entry.getKey()/*,
                       (File)entry.getValue()*/);
    }
    /**
     * 
     * @param oldF
     */
    public void removeFilePair(File oldF)
    {
        if (useFilePairs)
        {
            filePairList.remove(oldF);
        }
        else throw new IllegalArgumentException("Pairs mode is not enabled");
    }
    /**
     * 
     * @return mode
     */
    public ConversionMode getConversionMode()
    {
        return mode;
    }
    /**
     * 
     * @param f
     */
    public void addInputFile(File f)
    {
        fileMode = true;
        if (!inputFileList.contains(f))
        {
            inputFileList.add(f);
        }
    }
    /**
     * 
     * @param fs
     */
    public void addInputFiles(File [] fs)
    {
        for (int i = 0; i< fs.length; i++)
        {
            addInputFile(fs[i]);
        }
    }
    /**
     * 
     * @param f
     */
    public void removeInputFile(File f)
    {
        if (!inputFileList.contains(f))
        {
            inputFileList.remove(f);
        }
    }
    /**
     * 
     * @return input/output file pairs
     */
    public Object [] getInputFileList()
    {
        if (useFilePairs)
        {
            return filePairList.entrySet().toArray();
        }
        return inputFileList.toArray();
    }
    /**
     * 
     * @param prefix
     */
    public void setOutputPrefix(String prefix)
    {
        outputPrefix = prefix;
        outputDir = new File(prefix);
        if (!outputDir.isDirectory())
        {
            outputDir = null;
        }
    }
    /**
     * 
     * @param input
     * @return true if input file list contains input
     */
    public boolean hasInputFile(File input)
    {
        return filePairList.containsKey(input);
    }
    /**
     * 
     * @param input
     * @return output file for specified input file
     */
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

    
    
    /**
     * Add a new converter to the list or replace the current converter if only
     * one converter is 
     * @param cc
     */
    public void addConverter(CharConverter cc)
    {
        // Note: at the moment the encoding is set just before conversion is 
        // called, so it shouldn't really be necessary to do it here.
        cc.setEncodings(iCharset, oCharset);
        if (!mode.hasStyleSupport())
        {
            // can only have one converter in text mode
            converterList.put(new FontStyle(""),cc);
        }
        else if (!converterList.containsValue(cc) &&
                 !converterList.containsKey(cc.getOldStyle()))
        {
            converterList.put(cc.getOldStyle(),cc);
        }
    }
    /**
     * 
     * @param cc
     */
    public void removeConverter(CharConverter cc)
    {
        if (!converterList.containsValue(cc))
        {
            converterList.remove(cc.getOldStyle());
        }
    }
    /**
     * remove all converters
     */
    public void removeAllConverters()
    {
        converterList.clear();
    }
    /**
     * @return converter list
     */
    public Collection<CharConverter> getConverters()
    {
        return converterList.values();
    }
    /**
     * 
     * @return true if conversion can be performed
     */
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
    /**
     * 
     * @return true if conversion is in progress
     */
    synchronized public boolean isRunning() { return running; }
    
    protected void initDocInterface() throws org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException
    {
        if (docInterface == null)
        {
            switch (mode.getId())
            {
//                case ConversionMode.OO_ID:
//                    OOMainInterface ooInterface = new OOMainInterface();
//                    ooInterface.setOnlyStylesInUse(onlyStylesInUse);
//                    docInterface = ooInterface;
//                    break;
                case ConversionMode.TEXT_ID:
                    docInterface = new TextParser();
                    break;
                case ConversionMode.TEX_ID:
                    docInterface = new TeXParser();
                    break;
                case ConversionMode.OD_ID:
                    docInterface = new OpenDocParser();
                    break;
                default:
                    if (mode instanceof ExtensionConversionMode)
                    {
                        docInterface = ((ExtensionConversionMode)mode).getDocInterface();
                    }
                    else System.out.println("Unknown mode" + mode.getId());
            }
            if (iCharset != null) docInterface.setInputEncoding(iCharset);
            if (oCharset != null) docInterface.setOutputEncoding(oCharset);
        }
        if (docInterface == null) 
        {
            running = false; 
            System.out.println("No document interface!");
            showWarning("Failed to intialise document interface");
            return;
        }
        docInterface.initialise();
    }
    
    public void run()
    {
        Iterator <File>i;
        //stop = false;
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
            notifier.beginTask(MessageUtil.getString("conversionInProgress"),
                               getFileCount());
            notifier.worked(0);
        }
        //promptMode = PROMPT_NO;
        Iterator<CharConverter> c = converterList.values().iterator();
        if (docInterface != null)
        {
            if (docInterface.getMode() != mode)
            {
                docInterface.destroy();
                docInterface = null;
            }
        }
        
        try 
        {
            // initialise converters
            while (c.hasNext())
            {
                CharConverter cc = c.next();
                cc.initialize();
            }
            
            boolean retry = false;
            File inputFile = null;
            while ((i.hasNext())&&(notifier.isCancelled() == false))
            {
                if (retry == false)
                {
                    inputFile = (File)i.next();
                    synchronized (this)
                    {
                        notifier.worked(++currentFileIndex);
                        status = inputFile.getName();
                        notifier.subTask(status);
                    }
                    if (docInterface == null)
                        initDocInterface();
                }
                else
                {
                    initDocInterface();
                }
                File outputFile = getOutputFile(inputFile);
                final String filePath = outputFile.getAbsolutePath();
                if (outputFile.exists())
                {
                    if (promptMode == IMessageDisplay.Option.YES || 
                        promptMode == IMessageDisplay.Option.NO)
                    {
                        promptMode = 
                            msgDisplay.showYesNoMessage(
                                MessageUtil.getString("Msg_OverwriteFile", filePath),
                                MessageUtil.getString("Msg_Overwrite"));
                        
                    }
                    if (promptMode.equals(IMessageDisplay.Option.NO) ||
                        promptMode.equals(IMessageDisplay.Option.NO_ALL))
                            continue;
                }
                try
                {
                    docInterface.parse(inputFile,outputFile,converterList, notifier); 
                    retry = false;
                    notifier.setFileStatus(inputFile, 
                                           MessageUtil.getString("Finished"));
                }
                catch (java.lang.Exception e) 
                {
                    e.printStackTrace();
                    notifier.setFileStatus(inputFile, 
                            MessageUtil.getString("ConversionError"));
                    // only retry if we have had at least one successful run
                    // since the last attempt
                    if (autoRetry && retry == false) 
                    {
                        retry = true;
                        docInterface.destroy();
                        docInterface = null;
                        continue;
                    }
                    retry = true;
                    final String errorMsg = e.getLocalizedMessage();
                    IMessageDisplay.Option option = 
                        msgDisplay.showYesNoMessage(
                            MessageUtil.getString("Msg_ConversionErrorDesc", 
                                    inputFile.getAbsolutePath(), 
                                    errorMsg),
                            MessageUtil.getString("Msg_ConversionError"));
                    
                    if (option == IMessageDisplay.Option.NO)
                    {
                        stopConversion();
                        // reset docInterface
                        docInterface.destroy();
                        docInterface = null;
                    }
                    else
                    {
                        if (option == IMessageDisplay.Option.YES_ALL) 
                            autoRetry = true;
                        docInterface.destroy();
                    }
                    
                }
                if (commandLine) System.out.print('.');
            } // while ((i.hasNext())&&(stop == false))
            
            if (docInterface != null)
            {
                docInterface.destroy();
                docInterface = null;
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
            e.printStackTrace();
            showWarning(e.getLocalizedMessage());
        }
        finally
        {
            if (docInterface != null)
            {
                docInterface.destroy();
                docInterface = null;
            }
            running = false;
        }
    }
    protected void showWarning(final String warningMsg)
    {
        
        msgDisplay.showWarningMessage(
                MessageUtil.getString("Msg_ConversionFailDesc",
                                      getFileBeingConverted(), warningMsg), 
                MessageUtil.getString("Msg_ConversionError"));
    }
    /**
     * 
     * @return index of file being processed
     */
    public synchronized int getCurrentFileIndex() { return currentFileIndex; }
    /**
     * 
     * @return number of input files
     */
    public synchronized int getFileCount() 
    {
        return fileCount;
    }
    /**
     * 
     * @return name of file being converted
     */
    public synchronized String getFileBeingConverted()
    {
        return status;
    }
    /**
     * request the conversion to stop
     */
    public synchronized void stopConversion()
    {
        notifier.setCancelled(true);
        if (docInterface != null) docInterface.abort();
    }
    /**
     * stop conversion if necessary and clean up converters
     */
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
    /**
     * 
     * @param iEnc
     */
    public void setInputEncoding(Charset iEnc) 
    {
        iCharset = iEnc;
        Iterator<TextStyle> i = converterList.keySet().iterator();
        while(i.hasNext())
        {
            converterList.get(i.next()).setEncodings(iCharset, oCharset);
        }
    }
    /**
     * 
     * @return input character set
     */
    public Charset getInputEncoding() { return iCharset; }
    /**
     * 
     * @param oEnc
     */
    public void setOutputEncoding(Charset oEnc) 
    { 
        oCharset = oEnc; 
        Iterator<TextStyle> i = converterList.keySet().iterator();
        while(i.hasNext())
        {
            converterList.get(i.next()).setEncodings(iCharset, oCharset);
        }
    }
    /**
     * 
     * @return output character set
     */
    public Charset getOutputEncoding() { return oCharset; }
    /**
     * 
     * @return description of progress
     */
    public synchronized String getProgressDesc()
    {
      if (docInterface != null)
        return docInterface.getStatusDesc();
      else return new String("");
    }
    /**
     * 
     * @param newMsgDisplay
     */
    public void setMessageDisplay(IMessageDisplay newMsgDisplay)
    {
        msgDisplay = newMsgDisplay;
    }
    /**
     * 
     * @param fm
     */
    public void setFileMode(boolean fm)
    {
        fileMode = fm;
    }
    /**
     * 
     * @return true if in file mode
     */
    public boolean isFileMode()
    {
        return fileMode;
    }
    /**
     * 
     * @param pn
     */
    public void setProgressNotifier(ProgressNotifier pn)
    {
        notifier = pn;
    }
}
