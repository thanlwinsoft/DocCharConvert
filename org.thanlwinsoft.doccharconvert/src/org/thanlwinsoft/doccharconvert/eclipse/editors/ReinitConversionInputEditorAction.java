/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionInputEditor;

/**
 * @author keith
 *
 */
public class ReinitConversionInputEditorAction extends Action implements
        IEditorActionDelegate
{

    private ConversionInputEditor mInputEditor = null;
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    @Override
    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        if (targetEditor instanceof ConversionInputEditor)
        {
            mInputEditor = (ConversionInputEditor)targetEditor;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action)
    {
        mInputEditor.reinit();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
        // nothing to do
    }

}
