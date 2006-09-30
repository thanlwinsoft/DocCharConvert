/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.swt.widgets.Composite;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionInputEditor;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionRunnable;
import org.thanlwinsoft.doccharconvert.eclipse.EclipseMessageDisplay;
import org.thanlwinsoft.doccharconvert.eclipse.Perspective;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.DocumentParserPage;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.ReverseConversion;
/**
 * @author keith
 *
 */
public class ConversionWizard extends Wizard
{
    StructuredSelection iSelection;
    StructuredSelection oSelection;
    BatchConversion conversion = null;
    ConverterPage converterPage = null;
    private IWorkbenchWindow wbWindow;
    private WizardDialog dialog;
    static final String DOC_PARSER_PAGE = "DOC_PARSER_PAGE";
    static final String CONVERTER_PAGE = "CONVERTER_PAGE";
    static final String ENCODING_PAGE = "ENCODING_PAGE";
    static final String FILE_SELECT_PAGE = "FILE_SELECT_PAGE";
    
    public ConversionWizard(IWorkbenchWindow window)
    {
        this.wbWindow = window;
        this.setWindowTitle(MessageUtil.getString("Wizard_Title"));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages()
    {
        conversion = new BatchConversion();
        conversion.setMessageDisplay(new EclipseMessageDisplay(this.getShell()));
        addPage(new DocumentParserPage(conversion));
        converterPage = new ConverterPage(conversion); 
        addPage(converterPage);
        addPage(new FileSelectionPage(conversion));
        addPage(new EncodingPage(conversion));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish()
    {
        if (wbWindow == null) return false;
        try
        {
            if (conversion.isFileMode() == false)
            {
                directInput();
            }
            else
            {
                IViewPart fileList = wbWindow.getActivePage().showView(Perspective.CONV_FILE_LIST, 
                        null, IWorkbenchPage.VIEW_ACTIVATE);
                
                ConversionFileListView listView = null; 
                if (fileList != null)
                {
                    listView = (ConversionFileListView)fileList; 
                    listView.setConversion(conversion);
                }
                else
                    MessageDialog.openError(wbWindow.getShell(), "Error", 
                            "View not found");
                Assert.isNotNull(dialog);
                
                ConversionRunnable runnable = new ConversionRunnable(conversion, listView);
                
                dialog.run(false, true, runnable);
            }
        } 
        catch (PartInitException e) 
        {
            MessageDialog.openError(wbWindow.getShell(), "Error", 
                    "Error opening view:" + e.getMessage());
        }
        catch (CoreException e)
        {
            MessageDialog.openError(wbWindow.getShell(), "Error", 
                    "Error opening view:" + e.getMessage());
        }
        catch (InvocationTargetException e)
        {
            MessageDialog.openError(wbWindow.getShell(), "Error", 
                    "Error converting:" + e.getMessage());
        }
        catch (InterruptedException e)
        {
            MessageDialog.openError(wbWindow.getShell(), "Error", 
                    "Error converting:" + e.getMessage());
        }
        return true;
    }
    
    protected void directInput() throws PartInitException, CoreException
    {
        Collection <CharConverter> converters = conversion.getConverters();
        // just use the first converter
        CharConverter cc = converters.iterator().next();
        
        
        
        IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject myProject = myWorkspaceRoot.getProject("DocCharConvertData");
        if (myProject.exists() == false)
        {
            myProject.create(null);
        }
        // open if necessary
        if (myProject.exists() && !myProject.isOpen())
           myProject.open(null);
        IFolder tmpFolder = myProject.getFolder("tmp");
        if (tmpFolder.exists() == false)
        {
            tmpFolder.create(true, true, null);
        }
    
        IEditorInput eInput = null;
        IFile tmpFile = null;
        if (tmpFolder.exists())
        {
            tmpFile = tmpFolder.getFile("DocCharConvertScratch.txt");
            byte [] dummy = { 0 };
            // tmpFile.delete(true, false, null);
            if (tmpFile.exists() == false)
            {
              
              tmpFile.create(new ByteArrayInputStream(dummy), true, null);
            }
            tmpFile.setCharset("UTF-8", null);   
            eInput = new FileEditorInput(tmpFile);
        }
        if (eInput != null)
        {
            IEditorPart ePart = wbWindow.getActivePage().getActiveEditor();
            if (ePart != null && ePart.getEditorInput() != null)
            {
                IEditorInput input = ePart.getEditorInput();
                if (input instanceof FileEditorInput)
                {
                    FileEditorInput fei = (FileEditorInput)input;
                    if (fei.getFile().equals(tmpFile) == false)
                    {
                        ePart = null;
                    }
                }
            }
            if (ePart == null)
            {
                ePart = wbWindow.getActivePage().openEditor(eInput,
                    "org.thanlwinsoft.doccharconvert.eclipse.ConversionInputEditor");
                wbWindow.getActivePage().setEditorAreaVisible(true);
            }
            wbWindow.getActivePage().showView(Perspective.CONVERSION_RESULT,
                    null, IWorkbenchPage.VIEW_VISIBLE);
            wbWindow.getActivePage().showView(Perspective.REVERSE_CONVERSION,
                    null, IWorkbenchPage.VIEW_VISIBLE);
            wbWindow.getActivePage().showView(Perspective.DEBUG_UNICODE,
                    null, IWorkbenchPage.VIEW_VISIBLE);
            
            
            if (ePart != null && ePart instanceof ConversionInputEditor)
            {
                ConversionInputEditor te = (ConversionInputEditor)ePart;
                if (converterPage.getConverters() != null &&
                    converterPage.getConverters().size() > 0)
                {
                    CharConverter reverse = 
                        ReverseConversion.get(converterPage.getConverters(), 
                                              (ChildConverter)cc);
                    te.setConversion(cc, reverse);
                }      
            }
            
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer)
    {
        
        super.createPageControls(pageContainer);
    }

    public void setDialog(WizardDialog dialog)
    {
        this.dialog = dialog;
    }
    
}
