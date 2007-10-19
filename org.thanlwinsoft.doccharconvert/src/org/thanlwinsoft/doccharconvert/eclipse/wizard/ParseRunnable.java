/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Display;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.eclipse.Notifier;

/**
 * @author keith
 *
 */
public class ParseRunnable implements IRunnableWithProgress
{
    ConverterXmlParser xmlParser = null;
    Display display = null;
    Vector <CharConverter> availableConverters = null;
    ListViewer viewer = null;
    public ParseRunnable(ConverterXmlParser conv, Display display, ListViewer viewer)
    {
        this.xmlParser = conv;
        this.display = display;
        this.viewer = viewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
    {
        
        xmlParser.setProgressNotifier(new Notifier(monitor));
        xmlParser.parse();
        availableConverters = xmlParser.getConverters();
        if (availableConverters != null && availableConverters.size() > 0 &&
            viewer != null)
        {
            display.asyncExec (new Runnable () {
                public void run () {
                    viewer.add(availableConverters.toArray());
                }
            });
        }
        monitor.done();
    }

}
