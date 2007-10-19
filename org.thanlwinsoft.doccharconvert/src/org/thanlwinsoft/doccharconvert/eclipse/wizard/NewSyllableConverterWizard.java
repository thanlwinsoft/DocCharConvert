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
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.thanlwinsoft.doccharconvert.converter.SyllableConverter;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.editors.DccxEditor;
import org.thanlwinsoft.doccharconvert.eclipse.editors.SyllableConverterEditor;
import org.thanlwinsoft.eclipse.PathUtil;
import org.thanlwinsoft.schemas.docCharConvert.Argument;
import org.thanlwinsoft.schemas.docCharConvert.ConverterClass;

public class NewSyllableConverterWizard extends Wizard implements INewWizard
{

    private IStructuredSelection mSelection = null;
    private NewDccxWizardPage mDccxPage = null;
    private IWorkbench mWorkbench;
    private NewSyllableConverterPage mSyllablePage;

    @Override
    public boolean performFinish()
    {
        if (mWorkbench == null)
        {
            mWorkbench = PlatformUI.getWorkbench();
        }
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IPath syllableFilePath = mSyllablePage.getContainerFullPath().append(
                mSyllablePage.getFileName());
        IFile syllableFile = root.getFile(syllableFilePath);
        IPath dccxFilePath = mDccxPage.getContainerFullPath().append(
                mDccxPage.getFileName());
        IFile dccxFile = root.getFile(dccxFilePath);
        IPath relative = PathUtil.relativePathOfAwrtFileB(syllableFilePath, dccxFilePath);
        try
        {
            ConverterClass cc = ConverterClass.Factory.newInstance();
            cc.setName(SyllableConverter.class.getCanonicalName());
            Argument arg = cc.addNewArgument();
            arg.setType("file");
            arg.setValue(relative.toString());
            mDccxPage.setConverterClass(cc);
            dccxFile.create(mDccxPage.getInitialContents(), IFile.NONE, null);
            FileEditorInput input = new FileEditorInput(dccxFile);
            mWorkbench.getActiveWorkbenchWindow().getActivePage().openEditor(
                    input, DccxEditor.ID);
            syllableFile.create(mSyllablePage.getInitialContents(), IFile.NONE,
                    null);
            input = new FileEditorInput(syllableFile);
            mWorkbench.getActiveWorkbenchWindow().getActivePage().openEditor(
                    input, SyllableConverterEditor.ID);
        }
        catch (PartInitException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "Error in NewDccxWizard", e);
        }
        catch (CoreException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "Error in NewDccxWizard", e);
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
        mDccxPage = new NewDccxWizardPage(mSelection);
        mSyllablePage = new NewSyllableConverterPage(mSelection, mDccxPage);
        addPage(mSyllablePage);
        addPage(mDccxPage);
    }

}
