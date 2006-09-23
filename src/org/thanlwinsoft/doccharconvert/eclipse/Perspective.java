package org.thanlwinsoft.doccharconvert.eclipse;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class Perspective implements IPerspectiveFactory 
{
    public final static String FOLDER_A = "DocCharConvert.FolderA";
    public final static String FOLDER_B = "DocCharConvert.FolderB";
    public final static String CONVERSION_RESULT = 
        "DocCharConvert.ConversionResult";
    public final static String REVERSE_CONVERSION = 
        "DocCharConvert.ReverseConversion";
    public final static String DEBUG_UNICODE = 
        "DocCharConvert.DebugUnicode";
    public final static String CONV_FILE_LIST = 
        "DocCharConvert.ConversionFileList";
    public void createInitialLayout(IPageLayout layout) 
    {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.getViewLayout(layout.getEditorArea()).setCloseable(false);
        //layout.addNewWizardShortcut("DocCharConvertEclipse.wizard");
        /*
        IFolderLayout folderB = layout.createFolder(FOLDER_B, 
                IPageLayout.BOTTOM, 0.25f, editorArea);*/
        IFolderLayout folderA = layout.createFolder(FOLDER_A, 
                IPageLayout.BOTTOM, 0.75f, editorArea);
        //folderA.addView(CONVERSION_RESULT);
        folderA.addPlaceholder(DEBUG_UNICODE);
        folderA.addPlaceholder(CONV_FILE_LIST);
        
        layout.addStandaloneView(REVERSE_CONVERSION,  false, 
                IPageLayout.BOTTOM, 0.6667f, editorArea);
        layout.addStandaloneView(CONVERSION_RESULT,  false, 
                IPageLayout.BOTTOM, 0.5f, editorArea);
        
	}
}
