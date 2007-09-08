/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

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
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.swt.widgets.Composite;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;
import org.thanlwinsoft.doccharconvert.converter.test.ConversionTester;
import org.thanlwinsoft.doccharconvert.converter.test.LogConvertedWords;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionInputEditor;
import org.thanlwinsoft.doccharconvert.eclipse.ConversionRunnable;
import org.thanlwinsoft.doccharconvert.eclipse.EclipseMessageDisplay;
import org.thanlwinsoft.doccharconvert.eclipse.Perspective;
import org.thanlwinsoft.doccharconvert.eclipse.PreferencesInitializer;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.DocumentParserPage;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.Config;
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
    DocumentParserPage parserPage = null;
    ConverterPage converterPage = null;
    FontConversionPage fontPage = null;
    private IWorkbenchWindow wbWindow;
    private WizardDialog dialog;
    ConversionRunnable runnable = null;
    
    public final static String DEFAULT_PROJECT = "DocCharConvertData";
    public final static String TXT_EXT = ".txt";
    
    static final String DOC_PARSER_PAGE = "DOC_PARSER_PAGE";
    static final String CONVERTER_PAGE = "CONVERTER_PAGE";
    static final String FONT_CONVERTER_PAGE = "FONT_CONVERTER_PAGE";
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
        conversion.setMessageDisplay(new EclipseMessageDisplay(wbWindow.getShell()));
        parserPage = new DocumentParserPage(conversion);
        addPage(parserPage);
        converterPage = new ConverterPage(conversion);
        addPage(converterPage);
        fontPage = new FontConversionPage(converterPage, conversion);
        addPage(fontPage);
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
            Vector <CharConverter> converters = null;
            Vector <CharConverter> availableConverters = null;
            if (conversion.isFileMode() == false ||
                conversion.getConversionMode().hasStyleSupport())
            {
                converters = fontPage.getSelectedConverters();
                Vector <ChildConverter> childConverters = converterPage.getChildConverters();
                availableConverters = new Vector <CharConverter>(childConverters.size());
                for (ChildConverter cc : childConverters)
                {
                    availableConverters.add(cc);
                }
            }
            else
            {
                converters = converterPage.getSelectedConverters();
                availableConverters = converterPage.getConverters();
            }
            conversion.removeAllConverters();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
            sdf.setTimeZone(TimeZone.getDefault());
            PreferencesInitializer prefs = new PreferencesInitializer();
            prefs.initializeDefaultPreferences();
            
            for (CharConverter cc : converters)
            {
                File logDir = new File(prefs.getPrefStore().getString(Config.LOG_FILE));
                if (!logDir.isDirectory())
                {
                    IFolder logFolder = createProjectFolder("log");
                    logDir = logFolder.getRawLocation().toFile();
                }
                if (parserPage.isDebugEnabled())
                    cc.setDebug(true, logDir);
                if (parserPage.logWords())
                {
                    LogConvertedWords wordLogger = new LogConvertedWords(cc);
                    File file = new File(logDir,"Words" + sdf.format(new Date()) + ".csv");
                    wordLogger.setWordFile(file);
                    cc = wordLogger;
                }
                if (parserPage.doReverseCheck())
                {
                    CharConverter reverse = 
                        ReverseConversion.get(availableConverters, cc);
                    ConversionTester ct = new ConversionTester(cc, reverse);
                    ct.setLogFile(new File(logDir, 
                        cc.getName() + sdf.format(new Date()) + ".csv"));
                    conversion.addConverter(ct);
                }
                else conversion.addConverter(cc);
            }
            if (conversion.isFileMode() == false)
            {
                if (!directInput())
                    return false;
            }
            else
            {
                IWorkbenchPage page = wbWindow.getActivePage();
                IViewPart fileList = page.showView(ConversionFileListView.ID, 
                        conversion.toString(), IWorkbenchPage.VIEW_ACTIVATE);
                
                ConversionFileListView listView = null; 
                if (fileList != null)
                {
                    listView = (ConversionFileListView)fileList;
                    listView.resetStatus();
                    listView.setConversion(conversion);
                    
//                    IViewReference viewRef = page.findViewReference(Perspective.CONV_FILE_LIST);
//                    if (viewRef != null && page.isPageZoomed() == false)
//                    {
//                        page.toggleZoom(viewRef);
//                    }
                }
                else
                    MessageDialog.openError(wbWindow.getShell(), "Error", 
                            "View not found");
                Assert.isNotNull(dialog);
                
                runnable = new ConversionRunnable(conversion, listView);
                // The runnable is now run in the Action that openned this wizard
                // after the wizard has closed.

                //PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
                //.busyCursorWhile(runnable);
                //dialog.run(true, true, runnable);
                //PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, true, runnable);
                
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
        
        return true;
    }
    
    public ConversionRunnable getRunnable()
    {
        return runnable;
    }
    
    protected boolean directInput() throws PartInitException, CoreException
    {
        Collection <CharConverter> converters = conversion.getConverters();
        // just use the first converter
        Iterator <CharConverter> i = converters.iterator();
        if (!i.hasNext())
            return false;
        CharConverter cc = i.next();
        IEditorInput eInput = null;
        IFile tmpFile = null;
        IFolder tmpFolder = createProjectFolder("tmp");
        if (tmpFolder.exists())
        {
            String filename = cc.getName() + TXT_EXT;
            tmpFile = tmpFolder.getFile(filename);
            byte [] dummy = { ' ' };
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
            if (ePart == null || !(ePart instanceof ConversionInputEditor))
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
            IWorkbenchPart part = wbWindow.getActivePage().getActivePart();
            if (part != null)
            {
                if (part instanceof IViewPart)
                {
                    IViewPart vp = (IViewPart)part;
                }
                IViewReference [] viewRefs = wbWindow.getActivePage().getViewReferences();
                for (IViewReference ref : viewRefs)
                {
                    if (wbWindow.getActivePage().getPartState(ref) == IWorkbenchPage.STATE_MAXIMIZED)
                    {
                        wbWindow.getActivePage().setPartState(ref, IWorkbenchPage.STATE_RESTORED);
                    }
                }
            }
            
            if (ePart != null && ePart instanceof ConversionInputEditor)
            {
                ConversionInputEditor te = (ConversionInputEditor)ePart;
                if (converterPage.getConverters() != null &&
                    converterPage.getConverters().size() > 0)
                {
                    CharConverter reverse = null;
                    reverse = ReverseConversion.get(converterPage.getConverters(), 
                                                    cc);
                    te.setConversion(cc, reverse);
                }      
            }
            
        }
        return true;
    }

    private IFolder createProjectFolder(String name) throws CoreException
    {
        IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject myProject = myWorkspaceRoot.getProject(DEFAULT_PROJECT);
        if (myProject.exists() == false)
        {
            myProject.create(null);
        }
        // open if necessary
        if (myProject.exists() && !myProject.isOpen())
           myProject.open(null);
        IFolder tmpFolder = myProject.getFolder(name);
        if (tmpFolder.exists() == false)
        {
            tmpFolder.create(true, true, null);
        }
        return tmpFolder;
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
