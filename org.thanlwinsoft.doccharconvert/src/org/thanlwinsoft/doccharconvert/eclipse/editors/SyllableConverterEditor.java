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

package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.eclipse.EditorUtils;
import org.thanlwinsoft.schemas.docCharConvert.DocCharConverterDocument;
import org.thanlwinsoft.schemas.docCharConvert.Style;
import org.thanlwinsoft.schemas.docCharConvert.Styles;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverterDocument;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;

/**
 * @author keith
 *
 */
public class SyllableConverterEditor extends MultiPageEditorPart
{
    private Composite parent;
    private org.thanlwinsoft.doccharconvert.converter.SyllableConverter mConverter = null;
    public final static String ID = "org.thanlwinsoft.doccharconvert.eclipse.editors.SyllableConverterEditor";
    private SyllableConverterDocument converterDoc;
    private boolean dirty;
    private Image classImage = null;
    private Image mappingImage = null;
    private FileEditorInput mFileInput = null;
    private Font [] mFonts = new Font[2];
    public SyllableConverterEditor()
    {
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.init(site, input);
        mFileInput = null;
        mFonts[0] = null;
        mFonts[1] = null;
        this.setPartName(input.getName());
        try
        {
            if (input instanceof FileEditorInput)
            {
                mFileInput = (FileEditorInput)input;
                mFileInput.getFile().refreshLocal(1, null);
                URL fileUrl = mFileInput.getFile().getLocationURI().toURL();
                mConverter = new org.thanlwinsoft.doccharconvert.converter.SyllableConverter(fileUrl);
                mConverter.logMapStatus();
                mConverter.initialize();
            }
            InputStream is = EditorUtils.getInputStream(this);
            if (is != null)
                converterDoc = SyllableConverterDocument.Factory.parse(is);

        }
        catch (CoreException e)
        {
            MessageDialog.openWarning(site.getShell(), 
                MessageUtil.getString("SyllableConverterEditor"), 
                e.getLocalizedMessage());
        }
        catch (XmlException e)
        {
            MessageDialog.openWarning(site.getShell(), 
                MessageUtil.getString("SyllableConverterEditor"), 
                e.getLocalizedMessage());
        }
        catch (IOException e)
        {
            MessageDialog.openWarning(site.getShell(), 
                MessageUtil.getString("SyllableConverterEditor"), 
                e.getLocalizedMessage());
        }
        catch (FatalException e)
        {
            MessageDialog.openWarning(site.getShell(), 
                    MessageUtil.getString("SyllableConverterEditor"), 
                    e.getLocalizedMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPageContainer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Composite createPageContainer(Composite parent)
    {
        this.parent = parent;
        return super.createPageContainer(parent);
    }

//    private void removePages()
//    {
//        for (int i = getPageCount(); i > -1; i--)
//        {
//            this.removePage(i);
//        }
//    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#createPages()
     */
    @Override
    protected void createPages()
    {
        if (converterDoc != null)
        {
            if (this.getContainer() != null)
                parent = this.getContainer();
            ImageDescriptor id = DocCharConvertEclipsePlugin.getImageDescriptor("/icons/ClassTable16.png");
            if (id != null)
            {
                classImage = id.createImage(parent.getDisplay());
            }
            id = DocCharConvertEclipsePlugin
                .getImageDescriptor("/icons/ConversionTable16.png");
            if (id != null)
            {
                mappingImage = id.createImage(parent.getDisplay());
            }
            id = DocCharConvertEclipsePlugin.getImageDescriptor("/icons/Convert16.png");
            SyllableConverter sc = converterDoc.getSyllableConverter();
            int pageIndex;
            try
            {
                pageIndex = addPage(new ScriptsEditorPart(this), 
                    this.getEditorInput());
                this.setPageText(pageIndex, MessageUtil.getString("Scripts"));
                if (id != null)
                {
                    this.setPageImage(pageIndex, id.createImage(parent.getDisplay()));
                }
            }
            catch (PartInitException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING, 
                    "Error loading ScriptsEditor" , e);
            }
            if (sc.getClasses() != null)
            {
                for (org.thanlwinsoft.schemas.syllableParser.Class clazz : 
                    sc.getClasses().getClass1Array())
                {
                    addClassTable(clazz);
                }
            }
            
            for (MappingTable mt : sc.getMappingTableArray())
            {
                addMappingTable(mt);
            }
        }
    }
    
    protected void addClassTable(org.thanlwinsoft.schemas.syllableParser.Class clazz)
    {
        int pageIndex;
        try
        {
            pageIndex = addPage(new ClassTableEditorPart(this, clazz), 
                this.getEditorInput());
            this.setPageText(pageIndex, clazz.getId());
            if (classImage != null)
            {
                this.setPageImage(pageIndex, classImage);
            }
        }
        catch (PartInitException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, 
                "Error loading ClassTableEditor" , e);
        }
    }
    
    protected void addMappingTable(MappingTable mt)
    {
        try
        {
            int pageIndex = addPage(new MappingTableEditorPart(this, mt), this.getEditorInput());
            this.setPageText(pageIndex, mt.getId());
            if (mappingImage != null)
            {
                this.setPageImage(pageIndex, mappingImage);
            }
        }
        catch (PartInitException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Error loading MappingTableEditor" , e);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#addPage(int, org.eclipse.ui.IEditorPart, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void addPage(int index, IEditorPart editor, IEditorInput input)
        throws PartInitException
    {
        super.addPage(index, editor, input);
        this.setPageText(index, editor.getTitle());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor)
    {
        
        if (this.getEditorInput() instanceof IFileEditorInput)
        {
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            options.setSavePrettyPrint();
            try
            {
                File f = EditorUtils.getFileFromInput(this);
                converterDoc.save(f, options);
                IFile wsFile = EditorUtils.getWsFileFromInput(this);
                if (wsFile != null)
                    wsFile.refreshLocal(1, monitor);
                URL fileUrl = wsFile.getLocationURI().toURL();
                mConverter = new org.thanlwinsoft.doccharconvert.converter.SyllableConverter(fileUrl);
                monitor.beginTask(MessageUtil.getString("CompilingSyllableConverter"), 1);
                mConverter.logMapStatus();
                mConverter.initialize();
                for (int i = 0; i < getPageCount(); i++)
                {
                    IEditorPart part = this.getEditor(i);
                    if (part instanceof MappingTableEditorPart)
                    {
                        ((MappingTableEditorPart)part).refresh();
                    }
                }
                monitor.worked(1);
                monitor.done();
                this.setDirty(false);
            }
            catch (IOException e)
            {
                monitor.setCanceled(true);
            }
            catch (CoreException e)
            {
                monitor.setCanceled(true);
            }
            catch (FatalException e)
            {
                monitor.worked(1);
                monitor.done();
                MessageDialog.openWarning(parent.getShell(),
                        MessageUtil.getString("SyllableConverterEditor"),
                        e.getLocalizedMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#createSite(org.eclipse.ui.IEditorPart)
     */
    @Override
    protected IEditorSite createSite(IEditorPart editor)
    {
        // TODO Auto-generated method stub
        return super.createSite(editor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#getEditorSite()
     */
    @Override
    public IEditorSite getEditorSite()
    {
        // TODO Auto-generated method stub
        return super.getEditorSite();
    }
    
    public SyllableConverterDocument getDocument()
    {
        return this.converterDoc;
    }
    
    protected void setDirty(boolean dirty)
    {
        if (this.dirty != dirty)
        {
            this.dirty = dirty;
            this.mConverter = null;// needs to await a recompile on save
            this.firePropertyChange(PROP_DIRTY);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.MultiPageEditorPart#isDirty()
     */
    @Override
    public boolean isDirty()
    {
        return this.dirty;
    }

    protected int getEditorIndex(IEditorPart part)
    {
        int i;
        for (i = 0; i < this.getPageCount(); i++)
        {
            if (this.getEditor(i).equals(part))
                break;
        }
        return i;
    }
    
    protected Font getFont(int side)
    {
        if (mFonts[side] != null)
            return mFonts[side];
        Script s = converterDoc.getSyllableConverter().getScriptArray(side);
        String faceName = s.getFont();
        Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        if (faceName == null)
        {
            IPath dccxPath = mFileInput.getFile().getFullPath().removeFileExtension().addFileExtension("dccx");
            IResource dccxRes = ResourcesPlugin.getWorkspace().getRoot().findMember(dccxPath);
            if (dccxRes instanceof IFile)
            {
                IFile dccxFile = (IFile)dccxRes;
                try
                {
                    InputStream is = dccxFile.getContents(true);
                    DocCharConverterDocument doc = DocCharConverterDocument.Factory.parse(is);
                    Styles styles = doc.getDocCharConverter().getStyles();
                    if (styles.sizeOfStyleArray() > 0)
                    {
                        Style style = styles.getStyleArray(0);
                        faceName = style.getFontArray(side).getName();
                    }
                }
                catch (CoreException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (XmlException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (faceName != null)
        {
            FontData fd = new FontData(faceName, font.getFontData()[0].getHeight(), SWT.NORMAL);
            font = new Font(parent.getDisplay(), fd);
            mFonts[side] = font;
        }

        return font;
    }
    protected org.thanlwinsoft.doccharconvert.converter.SyllableConverter getConverter()
    {
        return mConverter;
    }
}
