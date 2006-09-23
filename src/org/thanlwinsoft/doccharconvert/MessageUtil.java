/**
 * 
 */
package org.thanlwinsoft.doccharconvert;
import java.text.MessageFormat;
/**
 * Message Utility for retrieving localised strings.
 * @author keith
 *
 */
public class MessageUtil
{
    static public String getString(String id)
    {
        return Config.getCurrent().getMsgResource().getString(id);
    }
    static public String getString(String id, String argA)
    {
        Object [] args = {argA};
        String baseString = Config.getCurrent().getMsgResource().getString(id);
        return MessageFormat.format(baseString, args);
    }
    static public String getString(String id, String argA, String argB)
    {
        Object [] args = {argA, argB };
        String baseString = Config.getCurrent().getMsgResource().getString(id);
        return MessageFormat.format(baseString, args);
    }
}
