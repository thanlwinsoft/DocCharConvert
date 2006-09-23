/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.eclipse.widgets.FileNameWidget;

import java.io.File;
import java.io.IOException;

/**
 * @author keith
 *
 */
public class FileSelectionPage extends WizardPage implements ModifyListener
{
    private FileNameWidget inputFile;
    private FileNameWidget outputFile;
    private File oldInputFile = null;
    private BatchConversion conversion = null;
    public FileSelectionPage(BatchConversion conversion)
    {
        super(ConversionWizard.FILE_SELECT_PAGE, 
              MessageUtil.getString("Wizard_FileSelectPageTitle"), null);
        this.conversion = conversion;
        this.conversion.setPairsMode(true);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Group mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
        RowLayout mainLayout = new RowLayout();
        mainLayout.type = SWT.VERTICAL;
        mainLayout.spacing = 5;
        mainLayout.fill = true;
        mainControl.setLayout(mainLayout);
        
        Label label = new Label(mainControl, SWT.LEFT | SWT.WRAP);
        label.setText(MessageUtil.getString("Wizard_SelectFiles"));
        inputFile = new FileNameWidget(mainControl, 
                MessageUtil.getString("Wizard_InputFile"), SWT.OPEN);
        inputFile.addModifyListener(this);
        outputFile = new FileNameWidget(mainControl, 
                MessageUtil.getString("Wizard_OutputFile"), SWT.SAVE);
        outputFile.addModifyListener(this);
        Config c = Config.getCurrent();
        try
        {
            inputFile.setDefaultPath(c.getInputPath().getCanonicalPath());
            outputFile.setDefaultPath(c.getOutputPath().getCanonicalPath());
        }
        catch (IOException e)
        {
            MessageDialog.openError(this.getShell(), "Error", 
                                    e.getMessage());
        }
        this.setControl(mainControl);
        setPageComplete(validatePage());
    }
    
    public void setPageComplete(boolean pc)
    {
        super.setPageComplete(pc);
    }
    
    protected boolean validatePage()
    {
        //if (conversion.isFileMode() == false) return true;
        
        if (oldInputFile != null)
            conversion.removeInputFile(oldInputFile);
        oldInputFile = null;
        if (inputFile.getFileName() != null &&
            outputFile.getFileName() != null &&
            inputFile.getFileName().length() > 0 &&
            outputFile.getFileName().length() > 0 &&
            inputFile.getFileName().equals(outputFile.getFileName()) == false)
        {
            File iFile = new File(inputFile.getFileName());
            File oFile = new File(outputFile.getFileName());
            conversion.addFilePair(iFile, oFile);
            Config.getCurrent().setInputPath(iFile.getParentFile());
            Config.getCurrent().setOutputPath(oFile.getParentFile());
            oldInputFile = iFile;
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    public void modifyText(ModifyEvent e)
    {
        // TODO Auto-generated method stub
        setPageComplete(validatePage());
    }
}
