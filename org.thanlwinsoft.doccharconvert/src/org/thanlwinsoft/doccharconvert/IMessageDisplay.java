/**
 * 
 */
package org.thanlwinsoft.doccharconvert;

/**
 * @author keith
 *
 */
public interface IMessageDisplay
{
    public enum Option { YES, NO, YES_ALL, NO_ALL };
    public void showWarningMessage(String message, String title);
    public Option showYesNoMessage(String message, String title);
}
