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
    public static ChildConverter get(Vector<ChildConverter>availableConverters,
                              ChildConverter cc)
    {
        ChildConverter rcc = null;
        if (cc.getParent() instanceof ReversibleConverter)
        {
            // try to find the reverse converter
            ReversibleConverter ccParent = 
                (ReversibleConverter)cc.getParent();
            int i = 0;
            while (rcc == null && i<availableConverters.size())
            {
                if (availableConverters.elementAt(i) != null)
                {
                    Object rco = availableConverters.elementAt(i);
                    if (rco instanceof CharConverter)
                    {
                        ChildConverter tempCc = null;
                        tempCc = (ChildConverter)rco;
                        if (tempCc.getParent() instanceof ReversibleConverter)
                        {
                            ReversibleConverter tempParent = 
                                (ReversibleConverter)tempCc.getParent();
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
