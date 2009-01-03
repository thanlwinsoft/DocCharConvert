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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.wizard.WizardDialog;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionRunnable;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class ConversionWizardAction implements IWorkbenchWindowActionDelegate 
{
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public ConversionWizardAction() 
    {
	    
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @param action 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) 
    {
		//      Create the wizard
        IWorkbenchWindow aww = window.getWorkbench().getActiveWorkbenchWindow();
                  
        ConversionWizard wizard = new ConversionWizard(window); 
        WizardDialog wizardDialog = 
            new WizardDialog(aww.getShell(), 
                wizard);
        wizardDialog.setMinimumPageSize(400, 300);
        wizard.setDialog(wizardDialog);
        wizardDialog.open();
        
//        try
//        {
            final ConversionRunnable runnable = wizard.getRunnable();
            if (runnable != null)
            {
                Job job = new Job(MessageUtil.getString("RunConversion")) {
                    public IStatus run(IProgressMonitor monitor) {
                        try
                        {
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
                job.setPriority(Job.LONG);
                job.schedule();
                //PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
            }
//        }
//        catch (InvocationTargetException e)
//        {
//            MessageDialog.openError(window.getShell(), "Error", 
//                    "Error converting:" + e.getMessage());
//        }
//        catch (InterruptedException e)
//        {
//            MessageDialog.openError(window.getShell(), "Error", 
//                    "Error converting:" + e.getMessage());
//        }
        // Not sure whether to close this automatically or not.
//        IIntroManager introManager = aww.getWorkbench().getIntroManager(); 
//        if (introManager.getIntro() != null)
//            introManager.closeIntro(introManager.getIntro());
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @param action 
	 * @param selection 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) 
    {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() 
    {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @param window 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) 
    {
		this.window = window;
	}
}