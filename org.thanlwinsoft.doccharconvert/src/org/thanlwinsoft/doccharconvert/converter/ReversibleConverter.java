/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.converter;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.test.ConversionTester;

/**
 *
 * @author  keith
 */
public abstract class ReversibleConverter implements CharConverter
{
    /**
     * @param isForwards
     */
    public void setDirection(boolean isForwards) { this.forwards = isForwards; }
    /**
     * @return true if in forwards direction
     */
    public boolean isForwards() { return forwards; }
    /**
     * 
     * @param aName
     */
    public void setOriginalStyle(TextStyle aName) { this.originalStyle = aName; }
    /**
     * @param aName
     */
    public void setTargetStyle(TextStyle aName) { this.targetStyle = aName; }
    /**
     * @return base name of converter - common to both directions
     */
    public abstract String getBaseName();
    /**
     * @param rName
     */
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
