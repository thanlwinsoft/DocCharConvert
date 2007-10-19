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

package org.thanlwinsoft.doccharconvert.eclipse.views;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionRunnable;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;

/**
 * @author keith
 *
 */
public class ReconvertSelectedAction extends Action implements IViewActionDelegate
{
    ConversionFileListView view = null;
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run()
    {
        if (view != null && view.getConversion() != null)
        {
            if (!view.getConversion().isRunning())
            {
                view.resetStatus();
                final BatchConversion selectedConversion = view.getSelectedConversion();
                Job job = new Job(MessageUtil.getString("RunConversion")) {
                    public IStatus run(IProgressMonitor monitor) {
                        try
                        {
                            ConversionRunnable runnable = new ConversionRunnable(selectedConversion, view);
                            runnable.run(monitor);
                        }
                        catch (InvocationTargetException e)
                        {
                            return new Status(IStatus.ERROR, 
                                DocCharConvertEclipsePlugin.ID, this.getName(), e);
                        }
                        catch (InterruptedException e)
                        {
                            return new Status(IStatus.ERROR, 
                                DocCharConvertEclipsePlugin.ID, this.getName(), e);
                        }
                        return new Status(IStatus.OK,
                            DocCharConvertEclipsePlugin.ID, this.getName());
                    }
                 };
                 job.setProperty(IProgressConstants.ICON_PROPERTY, 
                     DocCharConvertEclipsePlugin.getImageDescriptor("/org/thanlwinsoft/doccharconvert/icons/Convert16.png"));
                 job.schedule();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event)
    {
        run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
        if (view instanceof ConversionFileListView)
            this.view = (ConversionFileListView)view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action)
    {
        run();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection)
    {
        // TODO Auto-generated method stub
        
    }

}
