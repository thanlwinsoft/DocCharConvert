/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.views;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author keith
 *
 */
public class ConversionResult extends ViewPart
{
    TextViewer textViewer = null;
    Composite parent = null;
    Document document = null;
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        this.parent = parent;
        textViewer = new TextViewer(parent, SWT.LEFT | SWT.WRAP);
        document = new Document();
        textViewer.setDocument(document);
        textViewer.setEditable(false);
    }
    
    public void setResult(String faceName, int fontSize, String result)
    {
        //int fontSize = JFaceResources.getDialogFont().getFontData()[0].getHeight();
        FontData fd = new FontData(faceName, fontSize, SWT.NORMAL);
        Font font = new Font(parent.getDisplay(), fd);
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
        // TODO Auto-generated method stub

    }

}
