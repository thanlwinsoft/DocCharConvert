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
            return id;
        }
    }
    static public String getString(String id, String argA)
    {
        ResourceBundle r = Config.getCurrent().getMsgResource();
        if (r == null) return id + " " + argA;
        Object [] args = {argA};
        try
        {
            String baseString = r.getString(id);
            return MessageFormat.format(baseString, args);
        }
        catch (MissingResourceException mre)
        {
            return id + " " + argA;
        }
    }
    static public String getString(String id, String argA, String argB)
    {
        ResourceBundle r = Config.getCurrent().getMsgResource();
        if (r == null) return id + " " + argA + " " + argB;
        Object [] args = {argA, argB };
        try
        {
            String baseString = r.getString(id);
            return MessageFormat.format(baseString, args);
        }
        catch (MissingResourceException mre)
        {
            return id + " " + argA + " " + argB;
        }
    }
}
