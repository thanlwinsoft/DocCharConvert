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

package org.thanlwinsoft.doccharconvert.converters.myanmar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.myanmar.MyanmarValidator;
import org.thanlwinsoft.myanmar.Validator;
import org.thanlwinsoft.util.IClassLoaderUtil;

/**
 * Myanmar Unicode Validator - tries to correct common typing errors
 * @author keith
 *
 */
public class MyanmarUnicodeValidator implements CharConverter
{
	//private IClassLoaderUtil mClassLoader = null;
	private boolean mDebug = false;
	private File mLogFile = null;
	private File mLogDir = null;
	private BufferedWriter mLogWriter = null;
	private String mName = "";
	private String mPrev = "";

	/**
	 * Constructor
	 */
	public MyanmarUnicodeValidator()
	{
		
	}
	
	@Override
	public String convert(String oldText) throws FatalException,
			RecoverableException
	{
		MyanmarValidator mv = new MyanmarValidator();
        BufferedReader inReader = new BufferedReader(new StringReader(oldText));
        StringWriter outWriter = new StringWriter();
        BufferedWriter bufferedOut = new BufferedWriter(outWriter);
        Validator.Status status = mv.validate(inReader, bufferedOut);
        try
        {
            inReader.close();
            bufferedOut.close();
            if (mLogWriter != null && status == Validator.Status.Invalid)
            {
            	mLogWriter.append(MyanmarConverterActivator.msg("Invalid"));
            	mLogWriter.append(mPrev);
            	mLogWriter.newLine();
            	mLogWriter.append(oldText);
            	mLogWriter.newLine();
            }
            outWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RecoverableException(e.getLocalizedMessage());
        }
        String corrected = outWriter.toString(); 
		if (corrected.length() > 20)
			mPrev = corrected;
		else
			mPrev += corrected;
		return outWriter.toString();
	}

	@Override
	public void destroy()
	{
		if (mLogFile != null && mLogWriter != null)
		{
			try
			{
				mLogWriter.close();
			}
			catch (IOException e)
			{
				DocCharConvertEclipsePlugin.log(2, "Failed to close Myanmar Unicode Validator log", e);
			}
			mLogWriter = null;
		}
	}

	@Override
	public String getName()
	{
		if (mName.length() == 0)
			return MyanmarConverterActivator.msg("MyanmarUnicodeValidator");
		return mName;
	}

	@Override
	public String toString()
	{
		return getName();
	}
	
	@Override
	public TextStyle getNewStyle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TextStyle getOldStyle()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize() throws FatalException
	{
		if (mDebug)
			setDebug(mDebug, mLogDir);
	}

	@Override
	public boolean isInitialized()
	{
		//Bundle b;
		return true;
	}

	@Override
	public void setClassLoader(IClassLoaderUtil loader)
	{
		//mClassLoader = loader;
	}

	@Override
	public void setDebug(boolean on, File logDir)
	{
		mDebug = on;
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		mLogDir = logDir;
		mLogFile = new File(logDir, "MyanmarUnicodeValidator" +
				df.format(new Date()) + ".log");
		try
		{
			if (mLogWriter != null)
				mLogWriter.close();
			if (mDebug)
				mLogWriter = new BufferedWriter(new FileWriter(mLogFile));
		}
		catch (IOException e)
		{
			DocCharConvertEclipsePlugin.log(IStatus.WARNING,
				"MyanmarUnicodeValidator debug log failed to open", e);
			e.printStackTrace();
		}
	}

	@Override
	public void setEncodings(Charset charset, Charset charset2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String newName)
	{
		mName = newName;
	}

}
