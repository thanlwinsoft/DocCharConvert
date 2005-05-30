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
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author keith
 */
public class ExceptionList
{
    HashMap<String, String> leftExceptions = null;
    HashMap<String, String> rightExceptions = null;
    File [] files = null; 
    int [] maxLength = null;
    boolean [] caseInsensitive = { false, false }; 
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
    }
    public void load() throws IOException
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
                if (line[i] == null) break;
                if (line[i].length() > maxLength[i]) 
                    maxLength[i] = line[i].length();
            }
            if (line[0] == null || line[1] == null) 
            {
              break;
            }
            if (caseInsensitive[0]) 
              leftExceptions.put(line[0].toLowerCase(), line[1]);
            else
              leftExceptions.put(line[0], line[1]);
            if (caseInsensitive[1]) 
              rightExceptions.put(line[1].toLowerCase(), line[0]);
            else
              rightExceptions.put(line[1], line[0]);
        } while (line[0] != null || line[1] != null);
        for (int i = 0; i<2; i++)
        {
            reader[i].close();
        }
    }
    public boolean isException(int side, String text)
    {
        assert(side == 0 || side == 1);
        if (side == 0) return leftExceptions.containsKey(text);
        return rightExceptions.containsKey(text);
    }
    public String convert(int side, String text)
    {
        assert(side == 0 || side == 1);
        if (side == 0) return leftExceptions.get(text);
        return rightExceptions.get(text);
    }
    public int getMaxExceptionLength(int side)
    {
        assert(side == 0 || side == 1);
        return maxLength[side];
    }
    public void ignoreCase(boolean left, boolean right)
    {
      caseInsensitive[0] = left;
      caseInsensitive[1] = right;
    }
}
