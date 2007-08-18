/**
 * 
 */
package org.thanlwinsoft.doccharconvert.converter.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;

/**
 * @author keith
 *
 */
public class ConversionTester implements CharConverter
{
    
    HashMap <String, List<MismatchContext>> mismatches;
    final CharConverter mForwards;
    final CharConverter mBackwards;
    private File logFile = null;
    public ConversionTester(CharConverter forwards, 
                            CharConverter backwards)
    {
        this.mForwards = forwards;
        this.mBackwards = backwards;
    }

    public void setLogFile(File file)
    {
        logFile = file;
    }
    
    /**
     * @param input
     * @param output
     * @throws RecoverableException 
     * @throws FatalException 
     */
    public void test(String input, String output) throws FatalException, RecoverableException
    {
        String reversed = mBackwards.convert(output);
        if (reversed.equalsIgnoreCase(input) == false)
        {
            logMismatch(input.toLowerCase(), output, reversed.toLowerCase());
        }
    }
    /** Log a mismatched reverse conversion.
     * @param input
     * @param output
     * @param reversed
     */
    private void logMismatch(String input, String output, String reversed)
    {
        int i = 0;
        while (i < input.length() && i < reversed.length() && 
               input.charAt(i) == reversed.charAt(i))
        {
            i++;
        }
        // try scanning from end
        int j = input.length() - 1;
        int k = reversed.length() - 1;
        while (j > i && k > i && input.charAt(j) == reversed.charAt(k))
        {
            --j; --k;
        }
        ++j; ++k; // back off to last match
        String beforeContext = input.substring(0, i);
        String orig = input.substring(i, j);
        String wrong = reversed.substring(i, k);
        String afterContext = input.substring(j);
        MismatchContext context = new MismatchContext(beforeContext, afterContext, wrong, output);
        if (!mismatches.containsKey(orig))
        {
            mismatches.put(orig, new ArrayList<MismatchContext>());
        }
        mismatches.get(orig).add(context);
    }
    /**
     * Write out the mismatches and their contexts to a stream.
     * This does not close the stream.
     * @param os
     * @throws IOException
     */
    public void dumpToStream(OutputStream os) throws IOException
    {
        Pattern multipleSpace = Pattern.compile("\\s+");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
        Iterator <Map.Entry<String, List<MismatchContext>>> i = mismatches.entrySet().iterator();
        writer.write("Prefix\tInput\tReversed\tSuffix\tConverted");
        writer.newLine();
        while (i.hasNext())
        {
            Map.Entry<String, List<MismatchContext>> entry = i.next();
            for (MismatchContext mc : entry.getValue())
            {
                String before = multipleSpace.matcher(mc.before).replaceAll(" ");
                String after = multipleSpace.matcher(mc.after).replaceAll(" ");
                String orig = multipleSpace.matcher(entry.getKey()).replaceAll(" ");
                String wrong = multipleSpace.matcher(mc.wrong).replaceAll(" ");
                String converted = multipleSpace.matcher(mc.converted).replaceAll(" ");
                
                writer.write(before);
                writer.write('\t');
                writer.write(orig);
                writer.write('\t');
                writer.write(wrong);
                writer.write('\t');
                writer.write(after);
                writer.write('\t');
                writer.write(converted);
                writer.newLine();
            }
        }
        writer.close();
    }
    
    public class MismatchContext
    {
        final String converted;
        final String before;
        final String after;
        final String wrong;
        public MismatchContext(String before, String after, String wrong, String converted)
        {
            this.before = new String(before);
            this.after = new String(after);
            this.wrong = new String(wrong);
            this.converted = new String(converted);
        }
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#convert(java.lang.String)
     */
    public String convert(String oldText) throws FatalException,
        RecoverableException
    {
        String output = mForwards.convert(oldText);
        test(oldText, output);
        return output;
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#destroy()
     */
    public void destroy()
    {
        if (logFile != null)
        {
            try
            {
                FileOutputStream fis = new FileOutputStream(logFile);
                dumpToStream(fis);
                fis.close();
            }
            catch (IOException e)
            {
                System.out.println(e);
            }
        }
        mForwards.destroy();
        mBackwards.destroy();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#getName()
     */
    public String getName()
    {
        return mForwards.getName() + " [Test]";
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#getNewStyle()
     */
    public TextStyle getNewStyle()
    {
        return mForwards.getNewStyle();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#getOldStyle()
     */
    public TextStyle getOldStyle()
    {
        return mForwards.getOldStyle();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#initialize()
     */
    public void initialize() throws FatalException
    {
        mForwards.initialize();
        mBackwards.initialize();
        this.mismatches = new HashMap <String, List<MismatchContext> > ();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#isInitialized()
     */
    public boolean isInitialized()
    {
        return mForwards.isInitialized();
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#setDebug(boolean)
     */
    public void setDebug(boolean on, File logDir)
    {
        mForwards.setDebug(on, null);
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#setEncodings(java.nio.charset.Charset, java.nio.charset.Charset)
     */
    public void setEncodings(Charset iCharset, Charset oCharset)
    {
        mForwards.setEncodings(iCharset, oCharset);
        mBackwards.setEncodings(oCharset, iCharset);
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.CharConverter#setName(java.lang.String)
     */
    public void setName(String newName)
    {
        mForwards.setName(newName);
    }
}
