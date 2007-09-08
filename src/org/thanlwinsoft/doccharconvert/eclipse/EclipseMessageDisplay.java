/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import org.thanlwinsoft.doccharconvert.IMessageDisplay;
import org.thanlwinsoft.doccharconvert.MessageUtil;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
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
        final String warningMessage = message;
        shell.getDisplay().asyncExec(new Runnable() {
            public void run()
            {
                //MessageDialog.openWarning(shell, title, message);
                MessageDialog dialog = new MessageDialog(shell, title, null, 
                    title, MessageDialog.WARNING, new String[] {
                    MessageUtil.getString("OK")
                }, 0){

                    /* (non-Javadoc)
                     * @see org.eclipse.jface.dialogs.MessageDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
                     */
                    @Override
                    protected Control createDialogArea(Composite parent)
                    {
                        ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
                        GridData layoutData = new GridData();
                        layoutData.widthHint = 200;
                        layoutData.heightHint = 200;
                        sc.setLayoutData(layoutData);
                        sc.setExpandHorizontal(true);
                        sc.setExpandVertical(true);
                        Composite c = new Composite(sc, SWT.NONE);
                        c.setLayout(new FillLayout());
                        Label l = new Label(c, SWT.WRAP | SWT.LEAD);
                        l.setText(warningMessage);
                        c.setSize(l.computeSize(SWT.DEFAULT, SWT.DEFAULT));
                        sc.setContent(c);
                        sc.setAlwaysShowScrollBars(true);
                        return sc;
                    }

                    /* (non-Javadoc)
                     * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
                     */
                    @Override
                    protected Control createCustomArea(Composite parent)
                    {
                        return super.createContents(parent);
                    }};
               dialog.open();
//                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
//                msgBox.setMessage(message);
//                msgBox.setText(title);
//                msgBox.open();
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
