/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.actions.ActionFactory;
import org.thanlwinsoft.doccharconvert.MessageUtil;
/**
 * @author keith
 *
 */
public class ConversionResult extends ViewPart
{
    TextViewer textViewer = null;
    Composite parent = null;
    Document document = null;
    IAction copyAction = null;
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        this.parent = parent;
        textViewer = new TextViewer(parent, SWT.LEFT | SWT.WRAP | SWT.H_SCROLL 
                | SWT.V_SCROLL);
        document = new Document();
        textViewer.setDocument(document);
        textViewer.setEditable(false);
        makeActions();
        getViewSite().getActionBars().setGlobalActionHandler(
                ActionFactory.COPY.getId(),
                copyAction);
    }
    
    /**
     * 
     */
    private void makeActions()
    {
//      properties
        copyAction = new Action() {
            public void run()
            {
                textViewer.getTextWidget().copy();
            }
        };
        copyAction.setText(MessageUtil.getString("copy.text"));
        copyAction.setToolTipText(MessageUtil.getString("copy.tooltip"));
        copyAction.setEnabled(false);
    }

    public void setResult(String faceName, int fontSize, String result)
    {
        //int fontSize = JFaceResources.getDialogFont().getFontData()[0].getHeight();
        FontData fd = new FontData(faceName, fontSize, SWT.NORMAL);
        Font font = new Font(parent.getDisplay(), fd);
        setResult(font, result);
    }
    
    public void setResult(Font font, String result)
    {
        textViewer.getTextWidget().setFont(font);
        document = new Document(result);
        textViewer.setDocument(document);
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        copyAction.setEnabled(true);
    }

}
