package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.schemas.syllableParser.Cluster;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverterDocument;

public class NewSyllableConverterPage extends WizardNewFileCreationPage
{
    public static final String EXTENSION = "sylx";
    final private NewDccxWizardPage mConfigPage;
    
    public NewSyllableConverterPage(IStructuredSelection s, NewDccxWizardPage configPage)
    {
        super("NewSyllableConverterPage", s);
        this.setTitle(MessageUtil.getString("NewSyllableConverter"));
        setFileExtension(EXTENSION);
        mConfigPage = configPage;
    }

    @Override
    protected String getNewFileLabel()
    {
        return MessageUtil.getString("NewSyllableConverterFile");
    }

    @Override
    protected InputStream getInitialContents()
    {
        SyllableConverterDocument doc = SyllableConverterDocument.Factory.newInstance();
        SyllableConverter converter = doc.addNewSyllableConverter();
        Script scriptA = converter.addNewScript();
        scriptA.setName(MessageUtil.getString("ScriptAName"));
        Cluster cA = scriptA.addNewCluster();
        cA.addNewComponent().setId(MessageUtil.getString("LeftComponent","1"));
        Script scriptB = converter.addNewScript();
        scriptB.setName(MessageUtil.getString("ScriptBName"));
        Cluster cB = scriptB.addNewCluster();
        cB.addNewComponent().setId(MessageUtil.getString("RightComponent","1"));
        converter.addNewClasses();
        String name = this.getFileName();
        int dot = name.indexOf('.');
        if (dot > -1) name = name.substring(0, dot);
        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("UTF-8");
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            doc.save(bos);
            bos.close();
        }
        catch (IOException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, "DCCX getInitialcontents", e);
        }
        
        return new ByteArrayInputStream(bos.toByteArray());
    }

    @Override
    protected boolean validatePage()
    {
        boolean ok = super.validatePage();
        if (ok)
        {
            String name = getFileName();
            int dotIndex = name.indexOf('.');
            if (dotIndex > -1)
            {
                name = name.substring(0, dotIndex);
            }
            mConfigPage.setFileName(name);
        }
        return ok;
    }
    
    
}
