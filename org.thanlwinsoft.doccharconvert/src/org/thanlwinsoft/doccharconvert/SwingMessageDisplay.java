/**
 * 
 */
package org.thanlwinsoft.doccharconvert;

import java.lang.reflect.InvocationTargetException;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * @author keith
 *
 */
public class SwingMessageDisplay implements IMessageDisplay
{
    public final static int PROMPT_YES = 0;
    public final static int PROMPT_NO = 1;
    public final static int OVERWRITE_ALL = 2;
    public final static int SKIP_ALL = 3;
    private final static String [] OPTIONS = {"Yes","No","Yes to all","No to all"};
    private int promptMode = PROMPT_NO;
    
    private Component dialog;
    
    SwingMessageDisplay(Component dialog)
    {
        this.dialog = dialog;
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showWarningMessage(java.lang.String)
     */
    public void showWarningMessage(String message, String title)
    {
        final String fMessage = message;
        final String fTitle = title;
        
        Runnable promptRunnable = new Runnable() {
            public void run()
            {
                JOptionPane.showMessageDialog(dialog,
                    fMessage,
                    fTitle, JOptionPane.WARNING_MESSAGE);
            }
        };
        SwingUtilities.invokeLater(promptRunnable);
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showYesNoMessage(java.lang.String)
     */
    public Option showYesNoMessage(String message, String title)
    {
        final String fMessage = message;
        final String fTitle = title;
        Runnable promptRunnable = new Runnable() {
            public void run()
            {
                int option =
                    JOptionPane.showOptionDialog(dialog, 
                        fMessage, fTitle,
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE,null,
                        OPTIONS,OPTIONS[PROMPT_NO]);
                setPromptMode(option);                                            
            }
        };
        try
        {
            SwingUtilities.invokeAndWait(promptRunnable);
        }
        catch (InterruptedException e) {}
        catch (InvocationTargetException ite)
        {
            System.out.println(ite.getMessage());
        }
        return Option.NO;
    }
    public synchronized void setPromptMode(int mode)
    {
        promptMode = mode;
    }
    public synchronized int getPromptMode(int mode)
    {
        return promptMode;
    }
}
