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

package DocCharConvert.Converter.syllable;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.text.MessageFormat;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import DocCharConvert.Converter.CharConverter;
import DocCharConvert.Config;

/**
 *
 * @author keith
 */
public class ExceptionList implements SyllableChecker
{
    public final static String COMMENT_CHAR = "#";
    public final static String DELIMIT_CHAR = "\t";
    
    HashMap<String, String> leftExceptions = null;
    HashMap<String, String> rightExceptions = null;
    File [] files = null; 
    int [] maxLength = null;
    boolean [] caseInsensitive = { false, false }; 
    StringBuffer duplicates = null;
    /** Empty constructor for use with SyllableChecker interface 
     * initialize will be called to set the excepions files.
     */
    public ExceptionList()
    {
        this.leftExceptions = new  HashMap<String, String> ();
        this.rightExceptions = new  HashMap<String, String> ();
        maxLength = new int[2];
        maxLength[0] = 0;
        maxLength[1] = 0;
        duplicates = new StringBuffer();
    }
    /** Creates a new instance of ExceptionList */
    public ExceptionList(File leftExceptionsFile, File rightExceptionsFile)
    {
        files = new File[2];
        this.leftExceptions = new  HashMap<String, String> ();
        this.rightExceptions = new  HashMap<String, String> ();
        files[0] = leftExceptionsFile;
        files[1] = rightExceptionsFile;
        maxLength = new int[2];
        maxLength[0] = 0;
        maxLength[1] = 0;
        duplicates = new StringBuffer();
    }
    public ExceptionList(File exceptionsFile)
    {
        files = new File[1];
        this.leftExceptions = new  HashMap<String, String> ();
        this.rightExceptions = new  HashMap<String, String> ();
        files[0] = exceptionsFile;
        maxLength = new int[2];
        maxLength[0] = 0;
        maxLength[1] = 0;
        duplicates = new StringBuffer();
    }
    public void load() throws IOException, CharConverter.FatalException
    {
      if (files.length == 1)
      {
        BufferedReader reader = new BufferedReader(new FileReader(files[0]));
        String line = null;
        do
        {
          line = reader.readLine();
          if (line == null) break; 
          if (line.startsWith(COMMENT_CHAR)) continue; // comment character
          String [] words = line.split(DELIMIT_CHAR);
          if (words.length != 2)
          {
            // should both be NULL at same time
            Object [] args = { line, words.length, "2" };
            String msg = 
              Config.getCurrent().getMsgResource().getString("wrongNumTokens");
            throw new CharConverter.FatalException(MessageFormat.format(msg, args));
          }
          else
          {
            maxLength[0] = words[0].length();
            maxLength[1] = words[1].length();
            if (caseInsensitive[0]) 
              addException(leftExceptions,words[0].toLowerCase(), words[1]);
            else
              addException(leftExceptions,words[0], words[1]);
            if (caseInsensitive[1]) 
              addException(rightExceptions, words[1].toLowerCase(), words[0]);
            else
              addException(rightExceptions, words[1], words[0]);
          }
        } while (line != null);
        reader.close();
        if (duplicates.length() > 0)
        {
          Object [] args = {duplicates.toString()};
          String msg = 
            Config.getCurrent().getMsgResource().getString("exceptionDuplicates");
          throw new CharConverter.FatalException(MessageFormat.format(msg, args));              
        }
      }
      else load2Files();
    }
    public void load2Files() throws IOException, CharConverter.FatalException
    {
        BufferedReader [] reader = new BufferedReader[2];
        for (int i = 0; i<2; i++)
        {
            reader[i] = new BufferedReader(new FileReader(files[i]));
        }
        String [] line = new String[2];
        do
        {
            for (int i = 0; i<2; i++)
            {
                line [i] = reader[i].readLine();
                if (line[i] == null) continue;
                if (line[i].length() > maxLength[i]) 
                    maxLength[i] = line[i].length();
            }
            if (line[0] == null || line[1] == null) 
            {
              break;
            }
            if (line[0].startsWith(COMMENT_CHAR) &&
                line[1].startsWith(COMMENT_CHAR))
              continue;
            if (caseInsensitive[0]) 
              addException(leftExceptions,line[0].toLowerCase(), line[1]);
            else
              addException(leftExceptions,line[0], line[1]);
            if (caseInsensitive[1]) 
              addException(rightExceptions, line[1].toLowerCase(), line[0]);
            else
              addException(rightExceptions, line[1], line[0]);
        } while (line[0] != null || line[1] != null);
        if (line[0] != line[1])
        {
          // should both be NULL at same time
          Object [] args = { files[0].getName(), files[1].getName() };
          String msg = 
            Config.getCurrent().getMsgResource().getString("exceptLinesWrong");
          throw new CharConverter.FatalException(MessageFormat.format(msg, args));
        }
        for (int i = 0; i<2; i++)
        {
            reader[i].close();
        }
        if (duplicates.length() > 0)
        {
          Object [] args = {duplicates.toString()};
          String msg = 
            Config.getCurrent().getMsgResource().getString("exceptionDuplicates");
          throw new CharConverter.FatalException(MessageFormat.format(msg, args));              
        }
    }
    /** 
     * add words to exception list and check for duplicates
     * @param list to add to 
     * @param a key
     * @param b value
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
        list.put(a,b);
      }
    }
    /**
     * test whether word is an exception 
     * @param side that the text is written in
     * @param text to test for an exception
     * @return true if text is found in exception list
     */
    public boolean isException(int side, String text)
    {
        assert(side == 0 || side == 1);
        if (side == 0) return leftExceptions.containsKey(text);
        return rightExceptions.containsKey(text);
    }
    /**
     * converts word using exception list
     * if word is case insensitve the caller should convert to lower 
     * case before calling this method.
     * @param side that the text is written in
     * @param text to test for an exception
     * @return converted word or null if text is not in list
     */
    public String convert(int side, String text)
    {
        assert(side == 0 || side == 1);
        if (side == 0) return leftExceptions.get(text);
        return rightExceptions.get(text);
    }
    /**
     * the maximum length of an exception
     * @param side to check
     * @return the length of the longest exception on that side
     */
    public int getMaxExceptionLength(int side)
    {
        assert(side == 0 || side == 1);
        return maxLength[side];
    }
    /**
     * Should the exception list be converted to lower case?
     * @param left true if left hand side is not case-sensitive
     * @param right true if right hand side is not case-sensitive
     */
    public void ignoreCase(boolean left, boolean right)
    {
      caseInsensitive[0] = left;
      caseInsensitive[1] = right;
    }
    /** 
    * apply the exceptions to the given Syllable list
    */
    public Vector <Syllable> checkSyllables(int oldSide, Vector <Syllable> syllables, boolean debug)
    {
      ExceptionList exceptionList = this;
      Vector <Syllable> parseOutput = syllables;
      for (int i = 0; i< parseOutput.size(); i++)
        {
            Syllable s = parseOutput.get(i);
            int exLength = 0;
            if (exceptionList != null)
            {
                int j = i;
                StringBuffer exTest = new StringBuffer();
                int lastExMatch = -1;
                String lastInput = null;
                String lastMatch = null;
                do
                {
                    if (caseInsensitive[oldSide])
                      exTest.append(parseOutput.get(j).getOriginalString().toLowerCase());
                    else
                      exTest.append(parseOutput.get(j).getOriginalString());
                    exLength += parseOutput.get(j).originalLength();
                    if (exceptionList.isException(oldSide, exTest.toString()))
                    {
                        lastExMatch = j - i;
                        lastMatch = exceptionList.convert(oldSide, exTest.toString());
                        if (debug)
                        {
                            System.out.println("Exception: " + exTest.toString() 
                                               + " -> " + lastMatch);
                        }
                    }
                } while (exLength < exceptionList.getMaxExceptionLength(oldSide) &&
                         ++j < parseOutput.size());
                // replace the syllables found in the exception list with one
                // "unknown" syllable
                if (lastExMatch > -1)
                {
                    Syllable exSyl = new Syllable(lastMatch);
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
    * @param array that is assumed to specify the files containing
    * the exceptions.
    * @return true if initialisation succeeded
    */
    public boolean initialize(Script [] scripts, Object [] args)
    {
      boolean initOk = false;
      files = new File[args.length];
      ignoreCase(scripts[0].ignoreCase(), scripts[1].ignoreCase());
      switch (args.length)
      {
        case 0:
          initOk = true;
          break;
        case 2:
          if (args[1] instanceof File)
            files[1] = (File)args[1];
          else files[1] = new File(args[1].toString());
          // deliberate fall through
        case 1:
          if (args[0] instanceof File)
            files[0] = (File)args[0];
          else files[0] = new File(args[0].toString());
          try
          {
            load();
            initOk = true;
          }
          catch (IOException e)
          {
            System.out.println(e.getMessage());
          }
          catch (CharConverter.FatalException e)
          {
            System.out.println(e.getMessage());
          }
          break;
        default:
          System.out.println("exceptionlist does not support " + args.length + " args");
      }
      return initOk;
    }
}
