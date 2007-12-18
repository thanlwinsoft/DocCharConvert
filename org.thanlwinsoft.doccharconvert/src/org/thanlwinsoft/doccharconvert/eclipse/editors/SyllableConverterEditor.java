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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.eclipse.EditorUtils;
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
    public final static String ID = "org.thanlwinsoft.doccharconvert.eclipse.editors.SyllableConverterEditor";
    private SyllableConverterDocument converterDoc;
    private boolean dirty;
    private Image classImage = null;
    private Image mappingImage = null;
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
        
        this.setPartName(input.getName());
        try
        {
            if (input instanceof FileEditorInput)
            {
                ((FileEditorInput)input).getFile().refreshLocal(1, null);
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

}
