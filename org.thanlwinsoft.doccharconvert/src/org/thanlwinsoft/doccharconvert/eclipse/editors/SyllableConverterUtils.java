/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.Classes;
import org.thanlwinsoft.schemas.syllableParser.Map;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.w3c.dom.Node;

/**
 * @author keith
 *
 */
public class SyllableConverterUtils
{
    public static String getComponentName(SyllableConverter sc, String ref)
    {
        for (Script s : sc.getScriptArray())
        {
            for (Component c : s.getCluster().getComponentArray())
            {
                if (c.getId().equals(ref))
                {
                    StringBuilder sb = new StringBuilder();
                    Node n = c.getDomNode().getFirstChild();
                    while (n != null)
                    {
                        sb.append(n.getNodeValue());
                        n = n.getNextSibling();
                    }
                    return sb.toString();
                }
            }
        }
        return ref;
    }
    public static String getCText(C c)
    {
        if (c == null)
            return "";
        if (c.isSetHex())
        {
            StringBuilder sb = new StringBuilder();
            for (String hex : c.getHex().split("\\s"))
            {
                int codePoint = Integer.parseInt(hex, 16);
                sb.append(Character.toChars(codePoint));
            }
            return sb.toString();
        }
        if (c.isSetClass1())
            return "<" + c.getClass1() + ">";
        return c.getStringValue();
    }
    
    public static String getCTextWithCodes(C c)
    {
        StringBuilder sb = new StringBuilder();
        String text = getCText(c);
        sb.append(text);

        if (text.length() > 0 && !c.isSetClass1())
        {
            sb.append(" [");
            for (int i = 0; i < text.length(); i++)
            {
                if (i > 0) sb.append(" ");
                sb.append("u");
                sb.append(Integer.toHexString(text.charAt(i)));
            }
            sb.append("]");
        }
        return sb.toString();
    }
    
    public static C getCFromMap(Map m, String ref)
    {
        for (C c : m.getCArray())
        {
            if (c.isSetR() && c.getR().equals(ref))
            {
                return c;
            }
        }
        return null;
    }
    
    public static Vector<String> getApplicableClasses(SyllableConverter sc, String colRef)
    {
        Classes classes =  sc.getClasses();
        Vector <String>classRefs = new Vector<String>(classes.sizeOfClass1Array());
        for (org.thanlwinsoft.schemas.syllableParser.Class c : classes.getClass1Array())
        {
            for (ComponentRef cr : c.getComponentArray())
            {
                if (colRef.equals(cr.getR()))
                {
                    classRefs.add(c.getId());
                }
            }
        }
        //return classRefs.toArray(new String[classRefs.size()]);
        return classRefs;
    }
    static Pattern sUniInputPattern = Pattern.compile("u([0-9a-fA-F]{4})(\\s+u([0-9a-fA-F]{4}))*");
    
    public static String parseUniInput(String input)
    {
        String modifiedInput = input;
        Matcher m = sUniInputPattern.matcher(input);
        if (m.matches())
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= m.groupCount(); i+=2)
                {
                    if (m.group(i) == null) break;
                    int codePoint = Integer.parseInt(m.group(i), 16);
                    sb.append(Character.toChars(codePoint));
                }
                modifiedInput = sb.toString();
            }
            catch (NumberFormatException e)
            {
                // do nothing
            }
        }
        return modifiedInput;
    }
}
