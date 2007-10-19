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
        ReversibleConverter rcc = null;
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
                                rcc = (ReversibleConverter)tempCc;
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
