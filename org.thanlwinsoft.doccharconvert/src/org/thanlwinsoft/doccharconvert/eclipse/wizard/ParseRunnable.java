/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Display;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.eclipse.Notifier;

/**
 * @author keith
 *
 */
public class ParseRunnable implements IRunnableWithProgress
{
    ConverterXmlParser xmlParser = null;
    Display display = null;
    Vector <CharConverter> availableConverters = null;
    ListViewer viewer = null;
    public ParseRunnable(ConverterXmlParser conv, Display display, ListViewer viewer)
    {
        this.xmlParser = conv;
        this.display = display;
        this.viewer = viewer;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException
    {
        
        xmlParser.setProgressNotifier(new Notifier(monitor));
        xmlParser.parse();
        availableConverters = xmlParser.getConverters();
        if (availableConverters != null && availableConverters.size() > 0 &&
            viewer != null)
        {
            display.asyncExec (new Runnable () {
                public void run () {
                    viewer.add(availableConverters.toArray());
                }
            });
        }
        monitor.done();
    }

}
