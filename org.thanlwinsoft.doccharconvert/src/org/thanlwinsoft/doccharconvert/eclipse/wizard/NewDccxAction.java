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
