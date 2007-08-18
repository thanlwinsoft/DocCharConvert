/**
 * 
 */
package org.thanlwinsoft.doccharconvert.converter.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.PreferencesInitializer;

/**
 * @author keith
 *
 */
public class LogConvertedWords extends ChildConverter
{
    private File wordFile = null;
    public final static String WORD_SEPARATOR = 
        "[\\p{Zs}\\p{Zp}\\p{Zl}\\p{Ps}\\p{Pe}\\p{Sm}\\p{So}]+";
    private Pattern delimiterPattern = null;
    public final static String WORD_SEPARATOR_KEY = "WordSeparator";
    public class ConvertedWord
    {
        final String converted;
        int count = 1;
        ConvertedWord(String converted)
        {
            this.converted = converted;
        }
        public int getCount() { return count; }
        public void increment() { ++count; }
        public final String getConverted() { return converted; }
    }
    private HashMap<String, ConvertedWord> wordMap = new HashMap<String, ConvertedWord> ();
    public LogConvertedWords(CharConverter parent)
    {
        super(parent.getOldStyle(), parent.getNewStyle(), parent);
        new PreferencesInitializer().initializeDefaultPreferences();
        
    }
    
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.ChildConverter#initialize()
     */
    @Override
    public void initialize() throws FatalException
    {
        String pattern = Config.getCurrent().getPrefs()
        .get(WORD_SEPARATOR_KEY, WORD_SEPARATOR);
        try
        {
            delimiterPattern = Pattern.compile(pattern);
        }
        catch (PatternSyntaxException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, 
                "Failed to compile Word separator pattern", e);
        }
        super.initialize();
    }

    public void setWordFile(File file)
    {
        wordFile = file;
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#convert(java.lang.String)
     */
    public String convert(String oldText) throws FatalException,
        RecoverableException
    {
        String converted = parent.convert(oldText);
        String [] words = delimiterPattern.split(oldText);
        for (String w : words)
        {
            if (w.length() == 0) continue;
            if (wordMap.containsKey(w))
            {
                wordMap.get(w).increment();
            }
            else
            {
                wordMap.put(w, new ConvertedWord(parent.convert(w)));
            }
        }
        return converted;
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#destroy()
     */
    public void destroy()
    {
        if (wordFile != null)
        {
            try
            {
                FileOutputStream fis = new FileOutputStream(wordFile);
                dumpToStream(fis);
                fis.close();
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
        parent.destroy();
    }

    /**
     * @param fis
     * @throws IOException 
     */
    private void dumpToStream(OutputStream os) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
        // Sort the words
        Iterator<String> i = new TreeSet<String>(wordMap.keySet()).iterator();
        writer.write("Word\tConverted\tCount");
        writer.newLine();
        while (i.hasNext())
        {
            String key = i.next();
            ConvertedWord cw = wordMap.get(key);
            writer.append(key);            
            writer.append('\t');
            writer.append(cw.getConverted());
            writer.append('\t');
            writer.append(Integer.toString(cw.getCount()));
            writer.newLine();
        }
        writer.close();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#setName(java.lang.String)
     */
    public void setName(String newName)
    {
        parent.setName(newName);
    }

}
