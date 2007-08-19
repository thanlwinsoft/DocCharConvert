/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;

/**
 * @author keith
 *
 */
public class Notifier extends ProgressNotifier
{
    IProgressMonitor monitor = null;
    ConversionFileListView convFileListView = null;
    int worked = 0;
    public Notifier(IProgressMonitor monitor)
    {
        this.monitor = monitor;   
    }
    public Notifier(IProgressMonitor monitor, ConversionFileListView view)
    {
        this.monitor = monitor;   
        this.convFileListView = view;
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.ProgressNotifier#beginTask(java.lang.String, int)
     */
    @Override
    public void beginTask(String name, int totalWork)
    {
        monitor.beginTask(name, totalWork + 1);
        worked = 0;
    }
    public void setCancelled(boolean cancel) { monitor.setCanceled(cancel);}
    public boolean isCancelled() { return monitor.isCanceled(); }
    public void worked(int work) 
    { 
        monitor.worked(work - worked);
        worked = work;
    }
    public void subTask(String task) { monitor.subTask(task); };
    public void setFileStatus(final File file, final String status) 
    {
        if (convFileListView != null)
        {
            convFileListView.getSite().getShell().getDisplay().asyncExec(
                new Runnable() 
                {
                    public void run()
                    {
                        convFileListView.updateStatus(file, status);
                    }
                }
            );
        }
    }
}
