/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.ConversionWizard;

/**
 * @author keith
 *
 */
public class ConversionRunnable implements IRunnableWithProgress
{
    private BatchConversion conversion = null;
    ConversionFileListView listView = null;
    public ConversionRunnable(BatchConversion conv, ConversionFileListView listView)
    {
        this.conversion = conv;
        this.listView = listView;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(final IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
    {
        conversion.setProgressNotifier(new Notifier(monitor, listView));
        conversion.run();
        //System.out.println("conversion finished");
        monitor.done();
        listView.getSite().getShell().getDisplay().asyncExec(
            new Runnable() 
            {
                public void run()
                {
                    
                    IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(ConversionWizard.DEFAULT_PROJECT);
                    
                    try
                    {
                        p.refreshLocal(3, monitor);
                    }
                    catch (CoreException e)
                    {
                        DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Refresh error", e);
                    }
                    
                }
            }
        );
    }

}
