/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;

/**
 * @author keith
 * 
 */
public class ConverterPage extends WizardPage implements SelectionListener
{
    List converterList = null;
    ListViewer converterViewer = null;
    Shell shell = null;
    Vector<CharConverter> selectedConverters = null;
    private BatchConversion conversion = null;
    private ConverterXmlParser xmlParser = null;

    public ConverterPage(BatchConversion conversion)
    {
        super(ConversionWizard.CONVERTER_PAGE, MessageUtil
                .getString("Wizard_ConverterTitle"), null);
        this.conversion = conversion;
        selectedConverters = new Vector<CharConverter>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        shell = parent.getShell();
        Group mainControl = new Group(parent, SWT.SHADOW_ETCHED_IN);
        mainControl.setText(MessageUtil.getString("Wizard_ConverterPrompt"));
        RowLayout mainLayout = new RowLayout();

        mainLayout.type = SWT.VERTICAL;
        mainLayout.fill = true;
        // mainLayout.spacing = 5;
        mainControl.setLayout(mainLayout);

        converterList = new List(mainControl, SWT.MULTI | SWT.V_SCROLL);
        converterViewer = new ListViewer(converterList);
        xmlParser = ConverterUtil.parseConverters(getWizard().getContainer(),
                parent.getShell(), converterViewer);

        converterList.setLayoutData(new RowData(SWT.DEFAULT, 200));
        converterList.addSelectionListener(this);
        // mainControl.pack();
        setControl(mainControl);
        setPageComplete(validatePage());
        converterList.setFocus();
    }

    public void addSelectionListener(SelectionListener sl)
    {
        converterList.addSelectionListener(sl);
    }

    public void removeSelectionListener(SelectionListener sl)
    {
        converterList.removeSelectionListener(sl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e)
    {
        setPageComplete(validatePage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e)
    {

    }

    protected boolean validatePage()
    {
        if (conversion.getConversionMode().hasStyleSupport() == false
                && conversion.isFileMode() == true)
            conversion.removeAllConverters();
        IStructuredSelection selection = (IStructuredSelection) converterViewer
                .getSelection();
        Iterator<?> is = selection.iterator();
        selectedConverters.clear();
        while (is.hasNext())
        {
            CharConverter cc = (CharConverter) is.next();
            // if (conversion.getConversionMode().hasStyleSupport() == false &&
            // conversion.isFileMode() == true)
            // {
            // //conversion.addConverter(cc);
            // conversion.addTestConverter(cc, availableConverters);
            // }
            selectedConverters.add(cc);
            return true;
        }
        if (selection.size() > 0)
        {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage()
    {
        if (conversion.isFileMode() == false)
        {
            return this.getWizard().getPage(
                    ConversionWizard.FONT_CONVERTER_PAGE);
        }
        else
            if (conversion.getConversionMode().hasStyleSupport())
            {
                return this.getWizard().getPage(
                        ConversionWizard.FONT_CONVERTER_PAGE);
            }
            else
            {
                return this.getWizard().getPage(
                        ConversionWizard.FILE_SELECT_PAGE);
            }
    }

    public Vector<CharConverter> getConverters()
    {
        return xmlParser.getConverters();
    }

    public Vector<ChildConverter> getChildConverters()
    {
        return xmlParser.getChildConverters();
    }

    public Vector<CharConverter> getSelectedConverters()
    {
        return selectedConverters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        converterList.setFocus();
    }

}
