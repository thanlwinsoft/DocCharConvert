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

package org.thanlwinsoft.doccharconvert;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
/**
 * Message Utility for retrieving localised strings.
 * @author keith
 *
 */
public class MessageUtil
{
    static public String getString(String id)
    {
        ResourceBundle r = Config.getCurrent().getMsgResource();
        if (r == null) return id;
        try
        {
            return r.getString(id);
        }
        catch (MissingResourceException mre)
        {
            System.out.println("Missing resource id " + id);
            return id;
        }
    }
    static public String getString(String id, String argA)
    {
        Object [] args = {argA};
        return getString(id, args);
    }
    static public String getString(String id, String argA, String argB)
    {
        Object [] args = {argA, argB };
        return getString(id, args);
    }
    static public String getString(String id, Object [] args)
    {
        ResourceBundle r = Config.getCurrent().getMsgResource();
        if (r == null) return fallBackString(id, args);
        try
        {
            String baseString = r.getString(id);
            return MessageFormat.format(baseString, args);
        }
        catch (MissingResourceException mre)
        {
            System.out.println("Missing resource id " + id);
            return fallBackString(id, args);
        }
    }
    static private String fallBackString(String id, Object [] args)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        for (Object o : args)
        {
            sb.append(' ');
            if (o != null)
                sb.append(o.toString());
        }
        return sb.toString();
    }
}
