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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author keith
 * Create a new SyllableConverter
 */
public class NewSyllableConverterAction extends Action implements
IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow mWindow = null;
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
        Wizard wizard = new NewSyllableConverterWizard();
        WizardDialog wizardDialog = 
            new WizardDialog(mWindow.getShell(), 
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
