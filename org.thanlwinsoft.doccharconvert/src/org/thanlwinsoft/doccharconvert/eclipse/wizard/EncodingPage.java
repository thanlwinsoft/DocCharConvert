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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Iterator;
import java.util.SortedMap;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 *
 */
public class EncodingPage extends WizardPage implements SelectionListener
{
    Combo inputCombo;
    Combo outputCombo;
    private BatchConversion conversion = null;
    public EncodingPage(BatchConversion conversion)
    {
        super(ConversionWizard.ENCODING_PAGE, 
              MessageUtil.getString("Wizard_EncodingPageTitle"), null);
        this.conversion = conversion;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        RowLayout layout = new RowLayout();
        Group pageComposite = new Group(parent, SWT.SHADOW_ETCHED_IN);
        pageComposite.setText(MessageUtil.getString("Wizard_Encoding"));
        layout.type = SWT.VERTICAL;
        pageComposite.setLayout(layout);
        
        Label label = new Label(pageComposite, SWT.WRAP);
        label.setText(MessageUtil.getString("Wizard_EncodingPageDesc"));
        
        SortedMap <String,Charset>charsets = Charset.availableCharsets();
        
        Label inputLabel = new Label(pageComposite, SWT.WRAP);
        inputLabel.setText(MessageUtil.getString("Wizard_InputEncoding"));
        inputCombo = new Combo(pageComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        
        Label outputLabel = new Label(pageComposite, SWT.WRAP);
        outputLabel.setText(MessageUtil.getString("Wizard_OutputEncoding"));
        outputCombo = new Combo(pageComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        
        Iterator <String> iCharset = charsets.keySet().iterator();
        while (iCharset.hasNext())
        {
            String c = iCharset.next();
            inputCombo.add(c);
            outputCombo.add(c);
        }
        inputCombo.addSelectionListener(this);
        outputCombo.addSelectionListener(this);
        inputCombo.select(inputCombo.indexOf("UTF-8"));
        
        outputCombo.select(outputCombo.indexOf("UTF-8"));
        pageComposite.pack();
        this.setControl(pageComposite);
        setPageComplete(validatePage());
    }
    protected boolean validatePage()
    {
        if (inputCombo.getSelectionIndex() > -1 &&
            outputCombo.getSelectionIndex() > -1)
        {
            try
            {
                String name = inputCombo.getItem(inputCombo.getSelectionIndex());
                Charset ic = Charset.forName(name);
                conversion.setInputEncoding(ic);
                name = outputCombo.getItem(outputCombo.getSelectionIndex());
                Charset oc = Charset.forName(name);
                conversion.setOutputEncoding(oc);
                return true;
            }
            catch (IllegalCharsetNameException e)
            {
                System.out.println(e.getLocalizedMessage());
            }
        }
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        // TODO Auto-generated method stub
        setPageComplete(validatePage());
    }
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {
        // TODO Auto-generated method stub
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            inputCombo.setFocus();
        }   
        super.setVisible(visible);
    }
    
}
