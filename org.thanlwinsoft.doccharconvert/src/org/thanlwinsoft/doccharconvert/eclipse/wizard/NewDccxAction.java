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
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class NewDccxAction extends Action implements
IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow mWindow = null;
    public NewDccxAction()
    {
        
    }
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void init(IWorkbenchWindow window)
    {
        mWindow = window;
    }

    @Override
    public void run(IAction action)
    {
        Wizard wizard = new NewDccxWizard();
        Shell shell = mWindow.getShell();
        if (shell.isDisposed())
        {
            shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        }
        WizardDialog wizardDialog = 
            new WizardDialog(shell, 
                wizard);
        wizardDialog.setMinimumPageSize(400, 300);
        wizardDialog.open();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
        //mSelection = selection;
    }
    
}
