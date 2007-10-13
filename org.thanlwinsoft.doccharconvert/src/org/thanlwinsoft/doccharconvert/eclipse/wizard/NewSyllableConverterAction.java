package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

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
