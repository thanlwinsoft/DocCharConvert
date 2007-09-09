/**
 * 
 */
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
