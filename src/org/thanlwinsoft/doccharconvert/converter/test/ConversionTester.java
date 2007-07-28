/**
 * 
 */
package org.thanlwinsoft.doccharconvert.converter.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;

/**
 * @author keith
 *
 */
public class ConversionTester
{
    
    HashMap <String, List<MismatchContext>> mismatches;
    final ReversibleConverter mForwards;
    final ReversibleConverter mBackwards;
    public ConversionTester(ReversibleConverter forwards, 
                            ReversibleConverter backwards)
    {
        this.mForwards = forwards;
        this.mBackwards = backwards;
        this.mismatches = new HashMap <String, List<MismatchContext> > ();
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
        if (reversed.equals(input) == false)
        {
            logMismatch(input, output, reversed);
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
        while (i < input.length() && i < output.length() && 
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
        MismatchContext context = new MismatchContext(beforeContext, afterContext, wrong);
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
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        Iterator <Map.Entry<String, List<MismatchContext>>> i = mismatches.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry<String, List<MismatchContext>> entry = i.next();
            writer.write(entry.getKey());
            for (MismatchContext mc : entry.getValue())
            {
                writer.write('\t');
                writer.write(mc.before);
                writer.write('\t');
                writer.write(mc.wrong);
                writer.write('\t');
                writer.write(mc.after);
                writer.newLine();
            }
        }
    }
    
    public class MismatchContext
    {
        final String before;
        final String after;
        final String wrong;
        public MismatchContext(String before, String after, String wrong)
        {
            this.before = before;
            this.after = after;
            this.wrong = after;
        }
    }
}
