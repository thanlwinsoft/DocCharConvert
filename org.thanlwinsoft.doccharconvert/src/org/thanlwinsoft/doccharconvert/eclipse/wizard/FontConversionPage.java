/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;

/**
 * @author keith
 *
 */
public class FontConversionPage extends WizardPage
{
    private List fontList;
    private ListViewer viewer;
    private Group mainControl;
    private ArrayList<ChildConverter> fontConverterArray;
    final private ConverterPage converterPage;
    final private BatchConversion conversion;
    private SelectionListener conversionListener;
    private Vector<CharConverter> selectedConverters = null;
    /**
     * @param pageName
     */
    protected FontConversionPage(ConverterPage converterPage, BatchConversion batchConversion)
    {
        super(ConversionWizard.FONT_CONVERTER_PAGE);
        this.conversion = batchConversion;
        this.converterPage = converterPage;
        this.fontConverterArray = new ArrayList<ChildConverter>();
        this.selectedConverters = new Vector<CharConverter>();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        this.setTitle(MessageUtil.getString("Wizard_FontConversionPage"));
        mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
        mainControl.setText(MessageUtil.getString("Wizard_FontConversionPrompt"));
        FillLayout mainLayout = new FillLayout();
        
        mainLayout.type = SWT.VERTICAL;
        //mainLayout.fill = true;
        mainControl.setLayout(mainLayout);
        
        fontList = new List(mainControl, SWT.MULTI | SWT.V_SCROLL);
        viewer = new ListViewer(fontList);
        setControl(mainControl);
        conversionListener = new SelectionListener(){

            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e)
            {
                populateFontList();
                validatePage();
            }
        };
        converterPage.addSelectionListener(conversionListener);
        fontList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e)
            {
                selectedConverters.clear();
                if (conversion.getConversionMode().hasStyleSupport() ||
                    conversion.isFileMode() == false)
                {
                    //conversion.removeAllConverters();
                    for (int i : fontList.getSelectionIndices())
                    {
                        selectedConverters.add(fontConverterArray.get(i));
                        //conversion.addConverter(fontConverterArray.get(i));
                    }
                    setPageComplete(fontList.getSelectionCount() > 0);
                }
            }
        });
        setPageComplete(false);
    }
    
    public void validatePage()
    {
        if (conversion.getConversionMode().hasStyleSupport() || 
            conversion.isFileMode() == false)
        {
            setPageComplete(fontList.getSelectionCount() > 0);
        }
        else
        {
            setPageComplete(true);
        }
    }

    public void populateFontList()
    {
        Vector <ChildConverter> childConverters = converterPage.getChildConverters();
        viewer.remove(fontConverterArray.toArray());
        fontList.removeAll();
        fontConverterArray.clear();
        ArrayList <String> labelArray = new ArrayList<String>();
        for (CharConverter cc : converterPage.getSelectedConverters())
        {
            for (ChildConverter child : childConverters)
            {
                if (child.getParent().equals(cc))
                {
                    fontConverterArray.add(child);
                    labelArray.add(MessageUtil.getString("FontPair", 
                        child.getOldStyle().getDescription(), 
                        child.getNewStyle().getDescription()));
                }
            }
        }
        viewer.add(labelArray.toArray());
        if (conversion.getConversionMode().hasStyleSupport() ||
            conversion.isFileMode() == false)
        {
            fontList.select(0);// select first by default
            selectedConverters.removeAllElements();
            selectedConverters.add(fontConverterArray.get(0));
            //conversion.removeAllConverters();
            //conversion.addConverter(fontConverterArray.get(0));
        }
        fontList.redraw();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose()
    {
        converterPage.removeSelectionListener(conversionListener);
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage()
    {
        if (conversion.isFileMode() == false)
        {
            return this.getWizard().getPage(ConversionWizard.ENCODING_PAGE);
        }
        return super.getNextPage();
    }
    
    public Vector<CharConverter> getSelectedConverters()
    {
        return selectedConverters;
    }
}
