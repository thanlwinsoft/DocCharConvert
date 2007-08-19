/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import org.thanlwinsoft.doccharconvert.IMessageDisplay;
import org.thanlwinsoft.doccharconvert.MessageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
/**
 * @author keith
 *
 */
public class EclipseMessageDisplay implements IMessageDisplay
{
    Shell shell = null;
    public EclipseMessageDisplay(Shell shell)
    {
        this.shell = shell;
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showWarningMessage(java.lang.String, java.lang.String)
     */
    public void showWarningMessage(final String message, final String title)
    {
        shell.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                msgBox.setMessage(message);
                msgBox.setText(title);
                msgBox.open();
            }
        });
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showYesNoMessage(java.lang.String, java.lang.String)
     */
    public Option showYesNoMessage(final String message, final String title)
    {
        MessageRunnable runnable = new MessageRunnable(message, title);        
        shell.getDisplay().syncExec(runnable);
        return runnable.status;
    }
    protected class MessageRunnable implements Runnable
    {
        Option status;
        String message;
        String title;
        public MessageRunnable(final String message, final String title)
        {
            this.message = message;
            this.title = title;
        }
        public void run()
        {
            String [] labels = 
            {
                MessageUtil.getString("Yes"), 
                MessageUtil.getString("No"),
                MessageUtil.getString("YesToAll"), 
                MessageUtil.getString("NoToAll")
            };
            //MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.CANCEL);
            MessageDialog dialog = new MessageDialog(shell, title, null,
                message, MessageDialog.QUESTION, labels, Option.NO.ordinal());
            switch (dialog.open())
            {
            case 0:
                status = Option.YES;
                break;
            case 1:
                status = Option.NO;
                break;
            case 2:
                status = Option.YES_ALL;
                break;
            case 3:
                status = Option.NO_ALL;
                break;
            default:
                 status = Option.NO;
            }
        }
        public Option getStatus() { return status; }
    }
}
