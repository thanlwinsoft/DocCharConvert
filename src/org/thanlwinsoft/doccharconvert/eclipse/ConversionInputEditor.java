/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionResult;

/**
 * @author keith
 *
 */
public class ConversionInputEditor extends TextEditor implements IDocumentListener
{
    private CharConverter charConverter = null;
    private CharConverter reverseConverter = null;
    private int fontSize = 12;
    public ConversionInputEditor()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextEditor#dispose()
     */
    @Override
    public void dispose()
    {
        if (charConverter != null)
        {
            charConverter.destroy();
        }
        if (getSourceViewer() != null && 
            getSourceViewer().getDocument() != null)
        {
            getSourceViewer().getDocument().removeDocumentListener(this);
        }
        // TODO Auto-generated method stub
        super.dispose();
    }

    public void setConversion(CharConverter cc, CharConverter rcc)
    {
        if (charConverter != null)
        {
            charConverter.destroy();
        }
        if (reverseConverter != null)
        {
            reverseConverter.destroy();
        }
        charConverter = cc;
        reverseConverter = rcc;
        setFont();
        if (getSourceViewer() != null && 
            getSourceViewer().getDocument() != null)
        {
            getSourceViewer().getDocument().addDocumentListener(this);
        }
    }

    protected void setFont()
    {
        if (charConverter != null)
        {
            String faceName = charConverter.getOldStyle().getFontName();
            StyledText textWidget = this.getSourceViewer().getTextWidget();
            if (textWidget == null) return;
            fontSize = textWidget.getFont().getFontData()[0].getHeight();
            FontData fd = new FontData(faceName, fontSize, SWT.NORMAL);
            Shell shell = this.getSite().getWorkbenchWindow().getShell();
            Font font = new Font(shell.getDisplay(), fd);
            textWidget.setFont(font);
            showConversion(getSourceViewer().getDocument());
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextEditor#updatePropertyDependentActions()
     */
    @Override
    protected void updatePropertyDependentActions()
    {
        super.updatePropertyDependentActions();
        setFont();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event)
    {
        showConversion(event.getDocument());
    }
    protected void showConversion(IDocument document)
    {
        if (charConverter == null) return;
        try
        {
            if (charConverter.isInitialized() == false)
            {
                charConverter.initialize();
            }
            if (charConverter.isInitialized() == false)
            {
                Shell shell = this.getSite().getWorkbenchWindow().getShell();
                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
                msgBox.setMessage(MessageUtil.getString("Converter_InitialisationFailed",
                        charConverter.getName()));
                msgBox.open();
                return;
            }
            String converted = charConverter.convert(document.get());
            ConversionResult cr = (ConversionResult)
                this.getSite().getPage().findView(Perspective.CONVERSION_RESULT);
            cr.setResult(charConverter.getNewStyle().getFontName(), 
                         fontSize, converted);
            cr = (ConversionResult)
                this.getSite().getPage().findView(Perspective.REVERSE_CONVERSION);
            String reversed = "";
            if (reverseConverter != null)
            {
                reverseConverter.initialize();
                if (reverseConverter.isInitialized())
                    reversed = reverseConverter.convert(converted);
            }
            cr.setResult(charConverter.getOldStyle().getFontName(), 
                         fontSize, reversed);
        }
        catch (CharConverter.RecoverableException e)
        {
            Shell shell = this.getSite().getWorkbenchWindow().getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
            msgBox.setMessage(e.getLocalizedMessage());
            msgBox.open();
        }
        catch (CharConverter.FatalException e)
        {
            Shell shell = this.getSite().getWorkbenchWindow().getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_ERROR);
            msgBox.setMessage(e.getLocalizedMessage());
            msgBox.open();
            // prevent further conversions
            charConverter = null;
        }
    }
    
}
