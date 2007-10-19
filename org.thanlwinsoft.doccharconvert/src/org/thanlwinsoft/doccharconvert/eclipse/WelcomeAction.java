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
