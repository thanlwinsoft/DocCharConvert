package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableXmlReader;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;

/**
 * Test if the XML file uses the Syllable Converter namespace
 * @author keith
 * Note this only applies to matching with an open editor.
 * @see IEditorMatchingStrategy 
 */
public class SyllableConverterMatchingStrategy implements
        IEditorMatchingStrategy
{
    final static String sNamespace = SyllableXmlReader.NAMESPACE_URI;

    /**
     * Constructor
     */
    public SyllableConverterMatchingStrategy()
    {
        
    }
    
    @Override
    public boolean matches(IEditorReference editorRef, IEditorInput input)
    {
        InputStream is = null;
        InputStreamReader isr = null;
        try
        {
            if (input instanceof IStorageEditorInput)
            {
                IStorageEditorInput storageInput = (IStorageEditorInput) input;
                is = storageInput.getStorage().getContents();
            }
            else if (input instanceof IURIEditorInput)
            {
                IURIEditorInput uriInput = (IURIEditorInput) input;
                is = uriInput.getURI().toURL().openStream();
            }
            if (is != null)
            {
                isr = new InputStreamReader(is, "UTF-8");
                char[] buffer = new char[512];
                int read = isr.read(buffer);
                if (read > 0)
                {
                    String header = new String(buffer, 0, read);
                    if (header.indexOf(sNamespace) > -1)
                    {
                        return true;
                    }
                }
            }
        }
        catch (MalformedURLException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, e.getMessage(), e);
        }
        catch (IOException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, e.getMessage(), e);
        }
        catch (CoreException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, e.getMessage(), e);
        }
        finally
        {
            if (isr != null)
            {
                try
                {
                    isr.close();
                }
                catch (IOException e)
                {
                    DocCharConvertEclipsePlugin.log(IStatus.WARNING, e
                            .getMessage(), e);
                }
            }
        }
        return false;
    }

}
