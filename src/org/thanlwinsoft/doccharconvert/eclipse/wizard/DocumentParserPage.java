/**
 * 
 */
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
        //label.setLayoutData(new RowData(100,100));
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
        //combo.setLayoutData(new RowData(100,20));
        pageComposite.pack();
        this.setControl(pageComposite);
        setPageComplete(validatePage());
    }
    protected boolean validatePage()
    {
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
                return true;
            }
        }
        else if (inputButton.getSelection())
        {
            combo.setEnabled(false);
            conversion.setFileMode(false);
            IWizardPage fileSelectPage = 
                getWizard().getPage(ConversionWizard.FILE_SELECT_PAGE);
            if (fileSelectPage instanceof FileSelectionPage)
            {
                ((FileSelectionPage)fileSelectPage).setPageComplete(true);
            }
            
            conversion.setConversionMode(ConversionMode.TEXT_MODE);
            return true;
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

}
