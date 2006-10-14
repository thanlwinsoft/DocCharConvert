package org.thanlwinsoft.doccharconvert.eclipse;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.thanlwinsoft.doccharconvert.MessageUtil;

public class WelcomeAction extends Action implements IWorkbenchWindowActionDelegate
{
    private IWorkbenchWindow window = null;
    public WelcomeAction()
    {
        this.setId("org.thanlwinsoft.doccharconvert.Welcome");
        this.setText(MessageUtil.getString("Welcome"));
        this.setDescription(MessageUtil.getString("WelcomeDesc"));
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run()
    {
        IIntroManager introManager = PlatformUI.getWorkbench().getIntroManager();
        introManager.showIntro(window, true);
    }

    public void dispose()
    {
        // TODO Auto-generated method stub
        
    }

    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }

    public void run(IAction action)
    {
        run();
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        // TODO Auto-generated method stub
        
    }
    
}
