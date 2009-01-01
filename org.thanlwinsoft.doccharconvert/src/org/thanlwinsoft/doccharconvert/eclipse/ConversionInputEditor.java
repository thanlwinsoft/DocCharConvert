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
package org.thanlwinsoft.doccharconvert.eclipse;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.ConversionHelper;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.ReverseConversion;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionResult;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.ConverterUtil;

/**
 * @author keith
 *
 */
public class ConversionInputEditor extends TextEditor implements IDocumentListener
{
    private CharConverter charConverter = null;
    private CharConverter reverseConverter = null;
    private int fontSize = 12;
    private boolean reinit = false;
    public ConversionInputEditor()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.TextEditor#createActions()
     */
    @Override
    protected void createActions()
    {
        // TODO Auto-generated method stub
        super.createActions();
        getEditorSite().getActionBars().setGlobalActionHandler(
                ActionFactory.COPY.getId(),
                this.getAction(ActionFactory.COPY.getId()));
        getEditorSite().getActionBars().setGlobalActionHandler(
                ActionFactory.CUT.getId(),
                this.getAction(ActionFactory.CUT.getId()));
        getEditorSite().getActionBars().setGlobalActionHandler(
                ActionFactory.PASTE.getId(),
                this.getAction(ActionFactory.PASTE.getId()));
        
        
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
        if (reverseConverter != null)
        {
            reverseConverter.destroy();
        }
        charConverter = null;
        reverseConverter = null;
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
        if (charConverter != null && charConverter.getOldStyle() != null)
        {
            String faceName = charConverter.getOldStyle().getFontName();
            StyledText textWidget = this.getSourceViewer().getTextWidget();
            if (textWidget == null) return;
            //fontSize = textWidget.getFont().getFontData()[0].getHeight();
            fontSize = Config.getCurrent().getTestFontSize();
            FontData fd = new FontData(faceName, fontSize, SWT.NORMAL);
            Shell shell = this.getSite().getWorkbenchWindow().getShell();
            Font font = new Font(shell.getDisplay(), fd);
            textWidget.setFont(font);
        }
        if (getSourceViewer() != null && 
            getSourceViewer().getDocument() != null)
        {
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
    public void reinit()
    {
        reinit = true;
        if (getSourceViewer() != null && 
                getSourceViewer().getDocument() != null)
        {
            showConversion(getSourceViewer().getDocument());
        }
    }
    protected void showConversion(IDocument document)
    {
        // TODO move the conversion parsing / initialization into a separate thread
        if (charConverter == null) 
        {
            // try to re-find the char converter
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            ConverterXmlParser xmlParser = 
                ConverterUtil.parseConverters(window,this.getEditorSite().getShell());
            
            if (this.getEditorInput() instanceof FileEditorInput)
            {
                String name = ((FileEditorInput)getEditorInput()).getFile()
                    .getLocation().removeFileExtension().lastSegment();
                for (CharConverter cc : xmlParser.getChildConverters())
                {
                    if (ConverterUtil.cononicalizeName(cc.getName()).equals(name))
                    {
                        setConversion(cc, ReverseConversion.get(xmlParser.getConverters(), cc));
                        break;
                    }
                }
            }
            if (charConverter == null)
                return;
        }
        try
        {
            if (reinit || (charConverter.isInitialized() == false))
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
            String original = document.get();
            String converted = charConverter.convert(original);
            ConversionResult cr = (ConversionResult)
                getSite().getPage().findView(Perspective.CONVERSION_RESULT);
            cr.setResult(charConverter.getNewStyle().getFontName(), 
                         fontSize, converted);
            cr = (ConversionResult)
                getSite().getPage().findView(Perspective.REVERSE_CONVERSION);
            String reversed = "";
            if (reverseConverter != null)
            {
                if (reinit || (!reverseConverter.isInitialized()))
                    reverseConverter.initialize();
                if (reverseConverter.isInitialized())
                    reversed = reverseConverter.convert(converted);
            }
            cr.setResult(charConverter.getOldStyle().getFontName(), 
                         fontSize, reversed);
            ConversionResult unicode = (ConversionResult)
                getSite().getPage().findView(Perspective.DEBUG_UNICODE);
            StringBuilder hexCodes = new StringBuilder();
            ConversionHelper.debugDump(original, hexCodes);
            hexCodes.append("\n");
            ConversionHelper.debugDump(converted, hexCodes);
            hexCodes.append("\n");
            ConversionHelper.debugDump(reversed, hexCodes);
            if (unicode != null)
                unicode.setResult(JFaceResources.getTextFont(), hexCodes.toString());

            reinit = false;
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        // TODO Auto-generated method stub
        super.init(site, input);
        
    }
    
}
