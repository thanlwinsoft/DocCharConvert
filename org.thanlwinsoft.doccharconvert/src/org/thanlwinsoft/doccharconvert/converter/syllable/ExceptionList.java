/*
 * Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $ $LastChangedBy: keith $ $LastChangedDate: $ $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert.converter.syllable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.text.MessageFormat;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * 
 * @author keith
 */
public class ExceptionList implements SyllableChecker
{
    public final static String COMMENT_CHAR = "#";
    public final static String DELIMIT_CHAR = "\t";

    private HashMap<String, String> leftExceptions = null;
    private HashMap<String, String> rightExceptions = null;
    private HashSet<String> mWordJoiners = new HashSet<String>();
    private URL[] files = null;
    private int[] maxLength = null;
    private boolean[] caseInsensitive = { false, false };
    private long loadTime = 0;
    private StringBuffer duplicates = null;

    /**
     * Empty constructor for use with SyllableChecker interface initialize will
     * be called to set the exceptions files.
     */
    public ExceptionList()
    {
        this.leftExceptions = new HashMap<String, String>();
        this.rightExceptions = new HashMap<String, String>();
        maxLength = new int[2];
        maxLength[0] = 0;
        maxLength[1] = 0;
        duplicates = new StringBuffer();
    }

    public ExceptionList(URL exceptionsFile)
    {
        files = new URL[1];
        this.leftExceptions = new HashMap<String, String>();
        this.rightExceptions = new HashMap<String, String>();
        files[0] = exceptionsFile;
        maxLength = new int[2];
        maxLength[0] = 0;
        maxLength[1] = 0;
        duplicates = new StringBuffer();
    }

    public void load() throws IOException, CharConverter.FatalException,
            URISyntaxException
    {
        this.leftExceptions = new HashMap<String, String>();
        this.rightExceptions = new HashMap<String, String>();
        if (files.length == 1)
        {
            if (files[0].getProtocol().equals("file"))
            {
                loadTime = new File(files[0].toURI()).lastModified();
            }
            BufferedReader reader = null;
            try
            {
                Charset utf8 = Charset.forName("UTF-8");
                InputStream fis = files[0].openStream();
                reader = new BufferedReader(new InputStreamReader(fis, utf8));
            }
            catch (IllegalCharsetNameException e)
            {
                System.out.println(e.getLocalizedMessage());
                // this will probably be wrong, but try it anyway
                reader = new BufferedReader(new InputStreamReader(files[0]
                        .openStream()));
            }

            String line = null;
            do
            {
                line = reader.readLine();
                if (line == null)
                    break;
                if (line.startsWith(COMMENT_CHAR))
                    continue; // comment character
                String[] words = line.split(DELIMIT_CHAR);
                if (words.length != 2)
                {
                    // should both be NULL at same time
                    Object[] args = { line, words.length, "2" };
                    String msg = Config.getCurrent().getMsgResource()
                            .getString("wrongNumTokens");
                    throw new CharConverter.FatalException(MessageFormat
                            .format(msg, args));
                }
                else
                {
                    maxLength[0] = Math.max(maxLength[0], words[0].length());
                    maxLength[1] = Math.max(maxLength[1], words[1].length());
                    if (caseInsensitive[0])
                        addException(leftExceptions, words[0].toLowerCase(),
                                words[1]);
                    else
                        addException(leftExceptions, words[0], words[1]);
                    if (caseInsensitive[1])
                        addException(rightExceptions, words[1].toLowerCase(),
                                words[0]);
                    else
                        addException(rightExceptions, words[1], words[0]);
                }
            } while (line != null);
            reader.close();
            if (duplicates.length() > 0)
            {
                Object[] args = { duplicates.toString() };
                String msg = Config.getCurrent().getMsgResource().getString(
                        "exceptionDuplicates");
                throw new CharConverter.FatalException(MessageFormat.format(
                        msg, args));
            }
        }
    }

    /**
     * add words to exception list and check for duplicates
     * 
     * @param list
     *            to add to
     * @param a
     *            key
     * @param b
     *            value
     */
    protected void addException(HashMap<String, String> list, String a, String b)
            throws CharConverter.FatalException
    {
        if (list.containsKey(a))
        {
            duplicates.append(a);
            duplicates.append(">");
            duplicates.append(b);
            duplicates.append("|");
            duplicates.append(list.get(a));
            duplicates.append('\n');
        }
        else
        {
            list.put(a, b);
        }
    }

    /**
     * test whether word is an exception
     * 
     * @param side
     *            that the text is written in
     * @param text
     *            to test for an exception
     * @return true if text is found in exception list
     */
    public boolean isException(int side, String text)
    {
        assert (side == 0 || side == 1);
        if (side == 0)
            return leftExceptions.containsKey(text);
        return rightExceptions.containsKey(text);
    }

    /**
     * converts word using exception list if word is case insensitve the caller
     * should convert to lower case before calling this method.
     * 
     * @param side
     *            that the text is written in
     * @param text
     *            to test for an exception
     * @return converted word or null if text is not in list
     */
    public String convert(int side, String text)
    {
        assert (side == 0 || side == 1);
        if (side == 0)
            return leftExceptions.get(text);
        return rightExceptions.get(text);
    }

    /**
     * the maximum length of an exception
     * 
     * @param side
     *            to check
     * @return the length of the longest exception on that side
     */
    public int getMaxExceptionLength(int side)
    {
        assert (side == 0 || side == 1);
        return maxLength[side];
    }

    /**
     * Should the exception list be converted to lower case?
     * 
     * @param left
     *            true if left hand side is not case-sensitive
     * @param right
     *            true if right hand side is not case-sensitive
     */
    public void ignoreCase(boolean left, boolean right)
    {
        caseInsensitive[0] = left;
        caseInsensitive[1] = right;
    }

    /**
     * apply the exceptions to the given Syllable list
     */
    public Vector<Syllable> checkSyllables(int oldSide,
            Vector<Syllable> syllables, boolean debug)
    {
        if (debug)
        {
            // reload if file has changed
            try
            {
                if (files[0].getProtocol().equals("file")
                        && new File(files[0].toURI()).lastModified() > loadTime)
                    load();
            }
            catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch (FatalException e)
            {
                System.out.println(e.getMessage());
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
        ExceptionList exceptionList = this;
        Vector<Syllable> parseOutput = syllables;
        for (int i = 0; i < parseOutput.size(); i++)
        {
            int exLength = 0;
            // it is sometimes incorrect to match against the exception list
            // when we are mid word, so check against the exception separators
            while (i > 0
                    && mWordJoiners.contains(parseOutput.get(i - 1)
                            .getOriginalString()))
            {
                i++;
            }
            if (exceptionList != null && i < parseOutput.size())
            {
                int j = i;
                StringBuffer exTest = new StringBuffer();
                int lastExMatch = -1;
                String lastMatch = null;
                do
                {
                    if (caseInsensitive[oldSide])
                        exTest.append(parseOutput.get(j).getOriginalString()
                                .toLowerCase());
                    else
                        exTest.append(parseOutput.get(j).getOriginalString());
                    exLength += parseOutput.get(j).originalLength();
                    if (exceptionList.isException(oldSide, exTest.toString()))
                    {
                        lastExMatch = j - i;
                        lastMatch = exceptionList.convert(oldSide, exTest
                                .toString());
                        if (debug)
                        {
                            System.out.println("Exception: "
                                    + exTest.toString() + " -> " + lastMatch);
                        }
                    }
                } while (exLength < exceptionList
                        .getMaxExceptionLength(oldSide)
                        && ++j < parseOutput.size());
                // if the next syllable is not a word end, then we may need to
                // ignore the match, since it has only matched mid word
                if (j + 1 < parseOutput.size()
                        && mWordJoiners.contains(parseOutput.get(j + 1)
                                .getOriginalString()))
                {
                    lastExMatch = -1;
                }
                // replace the syllables found in the exception list with one
                // "unknown" syllable
                if (lastExMatch > -1)
                {
                    Syllable exSyl = new Syllable(parseOutput.get(i)
                            .getPrevious(), lastMatch);
                    do
                    {
                        parseOutput.remove(i);
                    } while (lastExMatch-- > 0);
                    parseOutput.insertElementAt(exSyl, i);
                    continue;
                }
            }
        }
        return parseOutput;
    }

    /**
     * Initialisation using SyllableChecker interface method.
     * 
     * @param array
     *            that is assumed to specify the files containing the
     *            exceptions.
     * @return true if initialisation succeeded
     */
    public boolean initialize(Script[] scripts, Object[] args)
            throws CharConverter.FatalException
    {
        boolean initOk = false;
        int fileCount = 0;
        while (fileCount < args.length && args[fileCount] instanceof URL)
            fileCount++;
        while (fileCount < args.length && args[fileCount] instanceof File)
            fileCount++;
        files = new URL[fileCount];
        ignoreCase(scripts[0].ignoreCase(), scripts[1].ignoreCase());
        switch (args.length)
        {
        case 0:
            initOk = true;
            break;
        case 3:
            addWordJoiners(args[2].toString());
        case 2:
            try
            {
                if (args[1] instanceof URL)
                    files[1] = (URL) args[1];
                else
                    if (args[1] instanceof File)
                        files[1] = ((File) args[1]).toURI().toURL();
                    else
                    {
                        addWordJoiners(args[1].toString());
                    }
            }
            catch (MalformedURLException e)
            {
                throw new CharConverter.FatalException(e.getMessage());
            }
            // deliberate fall through
        case 1:
            try
            {
                if (args[0] instanceof URL)
                    files[0] = (URL) args[0];
                else
                    if (args[0] instanceof File)
                        files[0] = ((File) args[0]).toURI().toURL();
                load();
                initOk = true;
            }
            catch (IOException e)
            {
                throw new CharConverter.FatalException(e.getMessage());
            }
            catch (URISyntaxException e)
            {
                throw new CharConverter.FatalException(e.getMessage());
            }
            break;
        default:
            throw new CharConverter.FatalException(
                    "exceptionlist does not support " + args.length + " args");
        }
        return initOk;
    }

    private void addWordJoiners(String separators)
    {
        String[] wordJoiners = separators.split("\\s+");
        for (String s : wordJoiners)
        {
            mWordJoiners.add(s);
        }
    }

    public boolean fileChanged()
    {
        for (int i = 0; i < files.length; i++)
        {
            try
            {
                if (files[i].getProtocol().equals("file")
                        && new File(files[i].toURI()).lastModified() > loadTime)
                    return true;
            }
            catch (URISyntaxException e)
            {
                // Ignore
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentDescriptions()
     */
    public String[] getArgumentDescriptions()
    {
        return new String[] { MessageUtil.getString("ExceptionListFile"),
                MessageUtil.getString("WordJoiners") };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentTypes()
     */
    public Class<?>[] getArgumentTypes()
    {
        return new Class<?>[] { URL.class, String.class };
    }
}
