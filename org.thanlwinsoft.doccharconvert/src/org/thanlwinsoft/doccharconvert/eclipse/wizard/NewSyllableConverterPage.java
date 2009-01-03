/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/
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
import org.thanlwinsoft.schemas.syllableParser.Side;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverterDocument;

/**
 * @author keith
 * Page to create a new Syllable Converter
 */
public class NewSyllableConverterPage extends WizardNewFileCreationPage
{
    /**
     * file extension of SyllableConverter configuration
     */
    public static final String EXTENSION = "xml";
    final private NewDccxWizardPage mConfigPage;
    
    /**
     * @param s
     * @param configPage
     */
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
        scriptA.setSide(Side.LEFT);
        scriptA.setName(MessageUtil.getString("ScriptAName"));
        Cluster cA = scriptA.addNewCluster();
        cA.setSide(Side.LEFT);
        cA.addNewComponent().setId(MessageUtil.getString("LeftComponent","1"));
        Script scriptB = converter.addNewScript();
        scriptB.setSide(Side.RIGHT);
        scriptB.setName(MessageUtil.getString("ScriptBName"));
        Cluster cB = scriptB.addNewCluster();
        cB.setSide(Side.RIGHT);
        cB.addNewComponent().setId(MessageUtil.getString("RightComponent","1"));
        converter.addNewChecks();
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
