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
package org.thanlwinsoft.eclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class EditorUtils
{
    public static File getFileFromInput(EditorPart editor) throws CoreException
    {
        File f = null;
        IFile wsFile = null;
        if (editor.getEditorInput() instanceof FileStoreEditorInput)
        {
            FileStoreEditorInput fsei = (FileStoreEditorInput)editor.getEditorInput();
            f = new File(fsei.getURI());
        }
        else if (editor.getEditorInput() instanceof FileEditorInput)
        {
            wsFile = ((FileEditorInput) editor.getEditorInput()).getFile();
            wsFile.refreshLocal(1, null);
            f = wsFile.getRawLocation().toFile();
        }
        return f;
    }
    
    public static IFile getWsFileFromInput(EditorPart editor)
    {
        IFile wsFile = null;
        if (editor.getEditorInput() instanceof FileEditorInput)
        {
            wsFile = ((FileEditorInput) editor.getEditorInput()).getFile();
        }
        return wsFile;
    }
    
    public static InputStream getInputStream(EditorPart editor) throws IOException, CoreException
    {
        InputStream is = null;
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IStorageEditorInput)
        {
            IStorageEditorInput sei = (IStorageEditorInput) input;
            is = sei.getStorage().getContents();
        }
        else if (input instanceof FileStoreEditorInput)
        {
            FileStoreEditorInput fsei = (FileStoreEditorInput)input;
            is = fsei.getURI().toURL().openStream();
        }
        return is;
    }
}
