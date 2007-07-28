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
        // If DocCharConvert is being used as a perspective inside normal
        // eclipse, it is best to show the editor
        layout.setEditorAreaVisible(true);
        layout.getViewLayout(layout.getEditorArea()).setCloseable(false);
        
        //layout.addNewWizardShortcut("DocCharConvertEclipse.wizard");
        
        IFolderLayout folderA = layout.createFolder(FOLDER_A, 
            IPageLayout.RIGHT, 0.7f, editorArea);
        folderA.addPlaceholder(DEBUG_UNICODE);
        folderA.addPlaceholder(CONV_FILE_LIST);
        folderA.addPlaceholder("org.eclipse.ui.internal.intro");
        
        layout.addStandaloneViewPlaceholder(CONVERSION_RESULT,  
            IPageLayout.BOTTOM, 0.5f, editorArea, true);

        layout.addStandaloneViewPlaceholder(REVERSE_CONVERSION,  
            IPageLayout.TOP, 0.5f, FOLDER_A, true);

        layout.addShowViewShortcut(CONVERSION_RESULT);
        layout.addShowViewShortcut(REVERSE_CONVERSION);
        layout.addShowViewShortcut(DEBUG_UNICODE);
        layout.addShowViewShortcut(CONV_FILE_LIST);
	}
}
