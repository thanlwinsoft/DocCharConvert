/**
 * 
 */
package org.thanlwinsoft.doccharconvert.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.SyllableConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;
import org.thanlwinsoft.doccharconvert.converter.syllable.ExceptionList;

/**
 * A dummy parser to test whether the entries in an ExceptionList are really
 * needed. 
 */
public class ExceptionListTestParser implements DocInterface
{
    final static String NEW_LINE = System.getProperty("line.separator");
    private boolean mAbort = false;
    private ConversionMode mMode = null;
    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#abort()
     */
    public void abort()
    {
        mAbort = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#destroy()
     */
    public void destroy()
    {
        // NOOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#getMode()
     */
    public ConversionMode getMode()
    {
        return mMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#getStatusDesc()
     */
    public String getStatusDesc()
    {
        return "Searching for unnecessary Exception list entries";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#initialise()
     */
    public void initialise() throws InterfaceException
    {
        // NOOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#parse(java.io.File,
     *      java.io.File, java.util.Map,
     *      org.thanlwinsoft.doccharconvert.ProgressNotifier)
     */
    public void parse(File input, File output,
        Map<TextStyle, CharConverter> converters, ProgressNotifier notifier)
        throws FatalException, InterfaceException, WarningException
    {
        mAbort = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try
        {
            Charset utf8 = Charset.forName("UTF-8");
            FileInputStream fis = new FileInputStream(input);
            reader = new BufferedReader(new InputStreamReader(fis, utf8));
            FileOutputStream fos = new FileOutputStream(output);
            writer = new BufferedWriter(new OutputStreamWriter(fos, utf8));
            String line = reader.readLine();
            StringBuilder unneeded = new StringBuilder();
            StringBuilder l2rOnly = new StringBuilder();
            StringBuilder r2lOnly = new StringBuilder();
            
            CharConverter left = createNonExceptionListConverter(converters, true);
            CharConverter right = createNonExceptionListConverter(converters, false);
            while (line != null && !mAbort)
            {
                String [] words = line.split(ExceptionList.DELIMIT_CHAR);
                // ignore comments
                if (line.startsWith("#") || words.length < 2)
                {
                    writer.write(line);
                    writer.newLine();
                    line = reader.readLine();
                    continue;
                }
                String left2right = left.convert(words[0]);
                String right2left = right.convert(words[1]);
                if (left2right.equals(words[1]))
                {
                    if (right2left.equals(words[0]))
                    {
                        // no need for exception
                        unneeded.append("#");
                        unneeded.append(line);
                        unneeded.append(NEW_LINE);
                    }
                    else
                    {
                        r2lOnly.append(line);
                        r2lOnly.append(NEW_LINE);
                    }
                }
                else if (right2left.equals(words[0]))
                {
                    l2rOnly.append(line);
                    l2rOnly.append(NEW_LINE);
                }
                else
                {
                    writer.write(line);
                    writer.newLine();
                }
                line = reader.readLine();
            }
            if (unneeded.length() > 0)
            {
                writer.append("## ");
                writer.write(MessageUtil.getString("Unneeded"));
                writer.newLine();
                writer.write(unneeded.toString());
            }
            if (l2rOnly.length() > 0)
            {
                writer.append("## ");
                writer.write(MessageUtil.getString("NeededLeftToRight"));
                writer.newLine();
                writer.write(l2rOnly.toString());
            }
            if (r2lOnly.length() > 0)
            {
                writer.append("## ");
                writer.write(MessageUtil.getString("NeededRightToLeft"));
                writer.newLine();
                writer.write(r2lOnly.toString());
            }
        }
        catch (IllegalCharsetNameException e)
        {
            throw new FatalException(e.getLocalizedMessage());
        }
        catch (FileNotFoundException e)
        {
            throw new FatalException(e.getLocalizedMessage());
        }
        catch (IOException e)
        {
            throw new FatalException(e.getLocalizedMessage());
        }
        catch (RecoverableException e)
        {
            throw new WarningException(e.getLocalizedMessage());
        }
        finally
        {
            try
            {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            }
            catch (IOException e)
            {
                throw new WarningException(e.getLocalizedMessage());
            }
        }
    }

    /**
     * @param converters
     * @param b
     * @return
     */
    private CharConverter createNonExceptionListConverter(
        Map<TextStyle, CharConverter> converters, boolean isForwards) 
        throws FatalException
    {
        TextStyle firstKey = converters.keySet().iterator().next();
        CharConverter cc = converters.get(firstKey);
        if (cc instanceof SyllableConverter)
        {
            SyllableConverter parent = (SyllableConverter)cc;
            SyllableConverter sc = new SyllableConverter(parent.getXmlFile());
            sc.initialize();
            sc.disableChecker(ExceptionList.class);
            sc.setDirection(isForwards);
            return sc;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#setInputEncoding(java.nio.charset.Charset)
     */
    public void setInputEncoding(Charset enc)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#setMode(org.thanlwinsoft.doccharconvert.ConversionMode)
     */
    public void setMode(ConversionMode mode)
    {
        mMode = mode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.DocInterface#setOutputEncoding(java.nio.charset.Charset)
     */
    public void setOutputEncoding(Charset enc)
    {
        // TODO Auto-generated method stub

    }

}
