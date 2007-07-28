/**
 * 
 */
package org.thanlwinsoft.doccharconvert;

import java.util.Vector;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;

/**
 * @author keith
 *
 */
public class ReverseConversion
{
    public static CharConverter get(Vector<CharConverter>availableConverters,
                              CharConverter conv)
    {
        CharConverter rcc = null;
        CharConverter cc = conv;
        if (cc instanceof ChildConverter)
            cc = ((ChildConverter)cc).getParent();
        if (cc instanceof ReversibleConverter)
        {
            // try to find the reverse converter
            ReversibleConverter ccParent = (ReversibleConverter)cc;
            int i = 0;
            while (rcc == null && i<availableConverters.size())
            {
                if (availableConverters.elementAt(i) != null)
                {
                    Object rco = availableConverters.elementAt(i);
                    if (rco instanceof CharConverter)
                    {
                        CharConverter tempCc = null;
                        tempCc = (CharConverter)rco;
                        if (tempCc instanceof ChildConverter)
                        {
                            tempCc = ((ChildConverter)tempCc).getParent();
                        }
                        if (tempCc instanceof ReversibleConverter)
                        {
                            ReversibleConverter tempParent = 
                                (ReversibleConverter)tempCc;
                            if (tempParent.getBaseName().equals(ccParent.getBaseName()) 
                               && (tempParent.isForwards() != 
                                   ccParent.isForwards()))
                            {
                                rcc = tempCc;
                                break;
                            }
                        }
                    }
                }
                i++;
            }
        }
        return rcc;
    }
}
