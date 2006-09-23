/**
 * 
 */
package org.thanlwinsoft.doccharconvert;


/**
 * @author keith
 *
 */
public class NoGuiMessageDisplay implements IMessageDisplay
{

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showWarningMessage(java.lang.String, java.lang.String)
     */
    public void showWarningMessage(String message, String title)
    {
        // TODO Auto-generated method stub
        System.out.println(title + "\t" + message);
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.IMessageDisplay#showYesNoMessage(java.lang.String, java.lang.String)
     */
    public Option showYesNoMessage(String message, String title)
    {
        // TODO Auto-generated method stub
        System.out.println(title + "\t" + message);
        return Option.YES_ALL;
    }

}
