/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;

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
public class Notifier extends ProgressNotifier
{
    IProgressMonitor monitor = null;
    ConversionFileListView convFileListView = null;
    int worked = 0;
    public Notifier(IProgressMonitor monitor)
    {
        this.monitor = monitor;   
    }
    public Notifier(IProgressMonitor monitor, ConversionFileListView view)
    {
        this.monitor = monitor;   
        this.convFileListView = view;
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.ProgressNotifier#beginTask(java.lang.String, int)
     */
    @Override
    public void beginTask(String name, int totalWork)
    {
        monitor.beginTask(name, totalWork + 1);
        worked = 0;
    }
    public void setCancelled(boolean cancel) { monitor.setCanceled(cancel);}
    public boolean isCancelled() { return monitor.isCanceled(); }
    public void worked(int work) 
    { 
        monitor.worked(work - worked);
        worked = work;
    }
    public void subTask(String task) { monitor.subTask(task); };
    public void setFileStatus(final File file, final String status) 
    {
        if (convFileListView != null)
        {
            convFileListView.getSite().getShell().getDisplay().asyncExec(
                new Runnable() 
                {
                    public void run()
                    {
                        convFileListView.updateStatus(file, status);
                    }
                }
            );
        }
    }
}
