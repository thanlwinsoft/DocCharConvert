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
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Composite;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.eclipse.ExtensionConversionMode;
/**
 * @author keith
 *
 */
public class DocumentParserPage extends WizardPage implements SelectionListener
{
    Combo combo = null;
    Button fileButton = null;
    Button inputButton = null;
    Button debug = null;
    Button wordLog = null;
    Button checkReverse = null;
    private BatchConversion conversion = null;
    public DocumentParserPage(BatchConversion conversion)
    {
        super(ConversionWizard.DOC_PARSER_PAGE, 
              MessageUtil.getString("Wizard_DocParserPageTitle"), null);
        this.conversion = conversion;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        
        RowLayout layout = new RowLayout();
        Group pageComposite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        pageComposite.setText(MessageUtil.getString("Wizard_DocumentMode"));
        layout.type = SWT.VERTICAL;
        pageComposite.setLayout(layout);
        inputButton = new Button(pageComposite, SWT.RADIO);
        inputButton.setText(MessageUtil.getString("Wizard_DirectInput"));
        inputButton.addSelectionListener(this);
        fileButton = new Button(pageComposite, SWT.RADIO);
        fileButton.setText(MessageUtil.getString("Wizard_FileInput"));
        fileButton.addSelectionListener(this);
        
        Label label = new Label(pageComposite, SWT.WRAP);
        label.setText(MessageUtil.getString("Wizard_DocParserPageDesc"));
        combo = new Combo(pageComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (int i = 0; i < ConversionMode.NUM_MODES; i++)
        {
            combo.add(ConversionMode.getById(i).toString());
        }
        ConversionMode [] extMode = ExtensionConversionMode.getExtensionModes();
        for (int i = 0; i < extMode.length; i++)
        {
            combo.add(extMode[i].toString());
        }
        combo.addSelectionListener(this);
        debug = new Button(pageComposite, SWT.CHECK);
        debug.setText(MessageUtil.getString("EnableLogging"));
        debug.setToolTipText(MessageUtil.getString("EnableLoggingTooltip"));
        wordLog = new Button(pageComposite, SWT.CHECK);
        wordLog.setText(MessageUtil.getString("EnableWordLogging"));
        wordLog.setToolTipText(MessageUtil.getString("EnableWordLoggingTooltip"));
        checkReverse = new Button(pageComposite, SWT.CHECK);
        checkReverse.setText(MessageUtil.getString("CheckReverse"));
        checkReverse.setToolTipText(MessageUtil.getString("CheckReverseTooltip"));
        
        pageComposite.pack();
        this.setControl(pageComposite);
        setPageComplete(validatePage());
    }
    public boolean isDebugEnabled()
    {
        return debug.getSelection();
    }
    public boolean logWords()
    {
        return wordLog.getSelection();
    }
    public boolean doReverseCheck()
    {
        return checkReverse.getSelection();
    }
    protected boolean validatePage()
    {
        if (combo.getSelectionIndex() > -1 && inputButton.getSelection() == false)
        {
            fileButton.setSelection(true);
        }
        if (fileButton.getSelection())
        {
            combo.setEnabled(true);
            if (combo.getSelectionIndex() > -1)
            {
                conversion.setFileMode(true);
                conversion.setConversionMode(ConversionMode.getById(combo.getSelectionIndex()));
                IWizardPage fileSelectPage = 
                    getWizard().getPage(ConversionWizard.FILE_SELECT_PAGE);
                if (fileSelectPage instanceof FileSelectionPage)
                {
                    ((FileSelectionPage)fileSelectPage).setPageComplete(false);
                }
                IWizardPage fontConversionPage = 
                    getWizard().getPage(ConversionWizard.FONT_CONVERTER_PAGE);
                if (fontConversionPage instanceof FontConversionPage)
                {
                    ((FontConversionPage)fontConversionPage).validatePage();
                }
                return true;
            }
        }
        else if (inputButton.getSelection())
        {
            combo.setEnabled(false);
            conversion.setFileMode(false);
            conversion.setConversionMode(ConversionMode.TEXT_MODE);
            IWizardPage fileSelectPage = 
                getWizard().getPage(ConversionWizard.FILE_SELECT_PAGE);
            if (fileSelectPage instanceof FileSelectionPage)
            {
                ((FileSelectionPage)fileSelectPage).setPageComplete(true);
            }
            
            IWizardPage fontConversionPage = 
                getWizard().getPage(ConversionWizard.FONT_CONVERTER_PAGE);
            if (fontConversionPage instanceof FontConversionPage)
            {
                ((FontConversionPage)fontConversionPage).validatePage();
            }
            
            return true;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        setPageComplete(validatePage());
    }
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {
        // NOOP
    }

}
