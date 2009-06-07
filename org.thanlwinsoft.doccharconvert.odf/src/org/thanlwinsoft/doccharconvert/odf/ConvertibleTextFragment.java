package org.thanlwinsoft.doccharconvert.odf;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.w3c.dom.Node;
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
