/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.Notifier;
import org.thanlwinsoft.doccharconvert.eclipse.PreferencesInitializer;
/**
 * @author keith
 *
 */
public class ConverterPage extends WizardPage implements SelectionListener
{
    List converterList = null;
    ListViewer converterViewer = null;
    Shell shell = null;
    Vector<CharConverter> availableConverters = null;
    Vector<CharConverter> selectedConverters = null;
    private BatchConversion conversion = null;
    private ConverterXmlParser xmlParser = null;

    public ConverterPage(BatchConversion conversion)
    {
        super(ConversionWizard.CONVERTER_PAGE, 
                MessageUtil.getString("Wizard_ConverterTitle"), null);
        this.conversion = conversion;
        selectedConverters = new Vector<CharConverter>();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        shell = parent.getShell();
        Group mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
        mainControl.setText(MessageUtil.getString("Wizard_ConverterPrompt"));
        RowLayout mainLayout = new RowLayout();
        
        mainLayout.type = SWT.VERTICAL;
        mainLayout.fill = true;
        //mainLayout.spacing = 5;
        mainControl.setLayout(mainLayout);
        
        converterList = new List(mainControl, SWT.MULTI | SWT.V_SCROLL);
        converterViewer = new ListViewer(converterList);
        
        parseConverters();
        
        converterList.setLayoutData(new RowData(SWT.DEFAULT, 200));
        converterList.addSelectionListener(this);
        //mainControl.pack();
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
        
    }
    protected boolean validatePage()
    {
        if (conversion.getConversionMode().hasStyleSupport() == false &&
            conversion.isFileMode() == true)
            conversion.removeAllConverters();
        IStructuredSelection selection = 
            (IStructuredSelection)converterViewer.getSelection();
        Iterator is = selection.iterator();
        selectedConverters.clear();
        while (is.hasNext())
        {
            CharConverter cc = (CharConverter)is.next();
            if (conversion.getConversionMode().hasStyleSupport() == false &&
                conversion.isFileMode() == true)
                conversion.addConverter(cc);
            selectedConverters.add(cc);
            return true;
        }
        if (selection.size() > 0)
        {
            return true;
        }
        return false;
    }
    
    public void parseConverters()
    {
        xmlParser = new ConverterXmlParser(getConverterPath());
        
        ParseRunnable pr = new ParseRunnable(xmlParser, 
                                             this.getShell().getDisplay());
        try
        {
            getWizard().getContainer().run(true, false, pr);
            if (xmlParser.getErrorLog().length() > 0)
            {
                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
                msgBox.setMessage(MessageUtil.getString("ConverterParseError") + 
                                  xmlParser.getErrorLog());
                msgBox.open();
            }
        }
        catch (InvocationTargetException e)
        {
            MessageDialog.openError(getShell(), "Error", 
                    "Error converting: InvocationTargetException " + 
                    e.getMessage());
        }
        catch (InterruptedException e)
        {
            MessageDialog.openError(getShell(), "Error", 
                    "Error converting: InterruptedException " + e.getMessage());
        }
        
    }
    
    static File getConverterPath()
    {
        File converterConfigPath = Config.getCurrent().getConverterPath();
        if (converterConfigPath.isDirectory() == false)
        {
            PreferencesInitializer initPref = new PreferencesInitializer();
            
            IPreferenceStore prefStore = initPref.getPrefStore();
            
            converterConfigPath = 
                new File(prefStore.getString(Config.CONVERTER_CONFIG_PATH));
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                            "Using pref store directly. " + 
                            converterConfigPath, null);
            
        }
        if (converterConfigPath.isDirectory() == false)
        {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
            msgBox.setMessage(MessageUtil.getString("NoConverterPath",
                              converterConfigPath.getAbsolutePath()));
            msgBox.open();
        }
        DocCharConvertEclipsePlugin.log(IStatus.INFO, "config dir:" + 
                converterConfigPath.getAbsolutePath(), null);
        System.out.println("Using config dir:" + 
            converterConfigPath.getAbsolutePath());
        return converterConfigPath;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    @Override
    public IWizardPage getNextPage()
    {
        if (conversion.isFileMode() == false)
        {
            return this.getWizard().getPage(ConversionWizard.FONT_CONVERTER_PAGE);
        }
        else if (conversion.getConversionMode().hasStyleSupport())
        {
            return this.getWizard().getPage(ConversionWizard.FONT_CONVERTER_PAGE);
        }
        else
        {
            return this.getWizard().getPage(ConversionWizard.FILE_SELECT_PAGE);
        }
    }
    
    public Vector<CharConverter> getConverters() 
    { 
        return availableConverters; 
    }
    
    public Vector<ChildConverter> getChildConverters()
    {
        return xmlParser.getChildConverters();
    }
    public Vector<CharConverter> getSelectedConverters()
    {
        return selectedConverters;
    }
    
    public class ParseRunnable implements IRunnableWithProgress
    {
        ConverterXmlParser xmlParser = null;
        Display display = null;
        public ParseRunnable(ConverterXmlParser conv, Display display)
        {
            this.xmlParser = conv;
            this.display = display;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException
        {
            
            xmlParser.setProgressNotifier(new Notifier(monitor));
            xmlParser.parse();
            availableConverters = xmlParser.getConverters();
            if (availableConverters != null && availableConverters.size() > 0)
            {
                display.asyncExec (new Runnable () {
                    public void run () {
                        converterViewer.add(availableConverters.toArray());
                    }
                });
            }
            monitor.done();
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        converterList.setFocus();
    }
    
}
