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
import org.thanlwinsoft.schemas.docCharConvert.ConverterClass;
import org.thanlwinsoft.schemas.docCharConvert.DocCharConverter;
import org.thanlwinsoft.schemas.docCharConvert.DocCharConverterDocument;

public class NewDccxWizardPage extends WizardNewFileCreationPage
{

    private ConverterClass mCC = null;

    public NewDccxWizardPage(IStructuredSelection s)
    {
        super("NewDccxPage", s);
        this.setTitle(MessageUtil.getString("NewConversionConfiguration"));
        setFileExtension("dccx");
    }

    @Override
    protected String getNewFileLabel()
    {
        return MessageUtil.getString("NewConversionConfigurationFile");
    }
    
    public void setConverterClass(ConverterClass cc)
    {
        mCC = cc;
    }

    @Override
    protected InputStream getInitialContents()
    {
        DocCharConverterDocument doc = DocCharConverterDocument.Factory.newInstance();
        DocCharConverter converter = doc.addNewDocCharConverter();
        String name = this.getFileName();
        int dot = name.indexOf('.');
        if (dot > -1) name = name.substring(0, dot);
        converter.setName(MessageUtil.getString("ConverterNameTemplate", name));
        converter.setRname(MessageUtil.getString("ConverterRNameTemplate", name));
        if (mCC != null)
            converter.setConverterClass(mCC);
        converter.addNewStyles();
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
    
    
}
