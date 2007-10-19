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
package org.thanlwinsoft.doccharconvert.eclipse;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.ConversionWizard;

/**
 * @author keith
 *
 */
public class ConversionRunnable implements IRunnableWithProgress
{
    private BatchConversion conversion = null;
    ConversionFileListView listView = null;
    public ConversionRunnable(BatchConversion conv, ConversionFileListView listView)
    {
        this.conversion = conv;
        this.listView = listView;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
    {
        conversion.setProgressNotifier(new Notifier(monitor, listView));
        conversion.run();
        //System.out.println("conversion finished");
        monitor.done();
        listView.getSite().getShell().getDisplay().asyncExec(
            new Runnable() 
            {
                public void run()
                {
                    
                    IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(ConversionWizard.DEFAULT_PROJECT);
                    
                    try
                    {
                        p.refreshLocal(3, monitor);
                    }
                    catch (CoreException e)
                    {
                        DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Refresh error", e);
                    }
                    
                }
            }
        );
    }

}
