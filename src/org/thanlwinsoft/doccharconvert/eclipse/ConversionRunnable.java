/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;

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
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
    {
        
        conversion.setProgressNotifier(new Notifier(monitor, listView));
        conversion.run();
        System.out.println("conversion finished");
        monitor.done();
    }

}
