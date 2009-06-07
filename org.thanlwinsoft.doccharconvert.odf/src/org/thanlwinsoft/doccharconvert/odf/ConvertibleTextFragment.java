/*
Copyright (C) 2009 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.odf;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.w3c.dom.Text;

/** 
 * Stores details of a fragment of a text node that can be converted
 * @author keith
 * 
 */
public class ConvertibleTextFragment
{
	private Text mText;
	private int mStart;
	private int mLength;
	private boolean mConverted;
	private CharConverter mConv;
	private ConvertibleTextFragment mPrev;
	/**
	 * @param text
	 * @param start
	 * @param length
	 * @param conv
	 * @param prevFrag
	 */
	public ConvertibleTextFragment(Text text, int start, int length, CharConverter conv, ConvertibleTextFragment prevFrag)
	{
		mText = text;
		mStart = start;
		mLength = length;
		mConv = conv;
		mPrev = prevFrag;
		mConverted = false;
	}
	/**
	 * @return text node
	 */
	public Text getNode() { return mText; }
	/**
	 * @return start index of fragment within text node
	 */
	public int getStart() { return mStart; }
	/**
	 * @return length of fragment
	 */
	public int getLength() { return mLength; }
	/**
	 * @return converter or null if no conversion needed
	 */
	public CharConverter getConverter() { return mConv; }
	/**
	 * @return previous fragment or null if none
	 */
	public ConvertibleTextFragment previousFragment() { return mPrev; }
	
	public String toString()
	{
		return mText.getData().substring(mStart, mStart + mLength);
	}
	/**
	 * @param newNode
	 */
	public void setNode(Text newNode)
	{
		mText = newNode;
	}
	/**
	 * @param i
	 */
	public void setStart(int i)
	{
		mStart = i;
	}
	/**
	 * @return true if already converted
	 */
	public boolean isConverted() { return mConverted; }
	/**
	 * @param converted
	 */
	public void setConverted(boolean converted) { mConverted = converted; }
}
