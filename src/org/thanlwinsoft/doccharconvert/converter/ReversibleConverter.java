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

package org.thanlwinsoft.doccharconvert.converter;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;
import org.thanlwinsoft.doccharconvert.converter.test.ConversionTester;

/**
 *
 * @author  keith
 */
public abstract class ReversibleConverter implements CharConverter
{
    public void setDirection(boolean isForwards) { this.forwards = isForwards; }
    public boolean isForwards() { return forwards; }
    public void setOriginalStyle(TextStyle aName) { this.originalStyle = aName; }
    public void setTargetStyle(TextStyle aName) { this.targetStyle = aName; }
    public abstract String getBaseName();
    public abstract void setReverseName(String rName);
    public TextStyle getOldStyle() 
    { 
        if(forwards==true) return originalStyle; 
        else return targetStyle; 
    }
    public TextStyle getNewStyle() 
    { 
        if(forwards==true) return targetStyle;
        return originalStyle; 
    }
    /**
     * Test that the result of the reverse conversion is the same as the forward
     * conversion.
     * @param input
     * @param output
     * @throws FatalException
     * @throws RecoverableException
     */
    protected void testConversion(String input, String output) throws FatalException, RecoverableException
    {
        if (tester != null)
            tester.test(input, output);
    }
    protected boolean forwards = true;
    protected  TextStyle originalStyle = null;
    protected  TextStyle targetStyle = null;
    protected ConversionTester tester = null;
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }
}
