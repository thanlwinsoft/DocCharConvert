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
