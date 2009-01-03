/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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
    protected final static int PROMPT_YES = 0;
    protected final static int PROMPT_NO = 1;
    protected final static int OVERWRITE_ALL = 2;
    protected final static int SKIP_ALL = 3;
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
    /**
     * 
     * @param mode
     */
    protected synchronized void setPromptMode(int mode)
    {
        promptMode = mode;
    }
    protected synchronized int getPromptMode(int mode)
    {
        return promptMode;
    }
}
