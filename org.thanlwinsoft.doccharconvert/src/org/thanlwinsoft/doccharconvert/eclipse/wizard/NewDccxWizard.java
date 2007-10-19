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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.editors.DccxEditor;

public class NewDccxWizard extends Wizard implements INewWizard
{

    private IStructuredSelection mSelection = null;
    private NewDccxWizardPage mPage = null;
    private IWorkbench mWorkbench;
    
    public NewDccxWizard() 
    {
        
    }

    @Override
    public boolean performFinish()
    {
        IPath filePath = mPage.getContainerFullPath().append(mPage.getFileName());
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
        if (mWorkbench == null)
        {
            mWorkbench = PlatformUI.getWorkbench();
        }
        try
        {
            file.create(mPage.getInitialContents(), IFile.NONE, null);
            FileEditorInput input = new FileEditorInput(file);
            mWorkbench.getActiveWorkbenchWindow().getActivePage().openEditor(input,
                  DccxEditor.ID);
        }
        catch (PartInitException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Error in NewDccxWizard", e);
        }
        catch (CoreException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Error in NewDccxWizard", e);
        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        mSelection = selection;
        mWorkbench = workbench;
    }

    @Override
    public void addPages()
    {
        if (mSelection == null)
            mSelection = new StructuredSelection();
        mPage = new NewDccxWizardPage(mSelection);
        addPage(mPage);
    }

}
