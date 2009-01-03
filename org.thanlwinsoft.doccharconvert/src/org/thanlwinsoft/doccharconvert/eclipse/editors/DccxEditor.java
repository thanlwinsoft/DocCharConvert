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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import java.io.InputStream;
import org.osgi.framework.Bundle;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.eclipse.EditorUtils;
import org.thanlwinsoft.eclipse.FileCellEditor;
import org.thanlwinsoft.eclipse.FontCellEditor;
import org.thanlwinsoft.schemas.docCharConvert.Age;
import org.thanlwinsoft.schemas.docCharConvert.ArgType;
import org.thanlwinsoft.schemas.docCharConvert.Argument;
import org.thanlwinsoft.schemas.docCharConvert.ConverterClass;
import org.thanlwinsoft.schemas.docCharConvert.DocCharConverter;
import org.thanlwinsoft.schemas.docCharConvert.DocCharConverterDocument;
import org.thanlwinsoft.schemas.docCharConvert.Font;
import org.thanlwinsoft.schemas.docCharConvert.Script;
import org.thanlwinsoft.schemas.docCharConvert.Style;
import org.thanlwinsoft.schemas.docCharConvert.Styles;

/**
 * @author keith
 * Editor for the DocCharConvert configuration XML file
 */
public class DccxEditor extends EditorPart
{
    private static final String HELP = "help";
    private static final int NAME_LIMIT = 50;
    private FormToolkit toolkit;
    private ScrolledForm form;
    private String CONVERTER_ELEMENT = "converter";
    private String CLASS_NAME = "class";
    private String NAME = "name";
    private boolean mDirty = false;
    private DocCharConverter mConverter = null;
    private TableViewer mParameterViewer = null;
    private DocCharConverterDocument mDoc;
    /**
     * editor id
     */
    public static String ID = "org.thanlwinsoft.doccharconvert.DccxEditor";

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        File f = null;
        IFile wsFile = null;
        if (getEditorInput() instanceof FileStoreEditorInput)
        {
            FileStoreEditorInput fsei = (FileStoreEditorInput)getEditorInput();
            f = new File(fsei.getURI());           
        }
        else if (getEditorInput() instanceof FileEditorInput)
        {
            wsFile = ((FileEditorInput) getEditorInput()).getFile();
            f = wsFile.getRawLocation().toFile();
        }
        XmlOptions options = new XmlOptions();
        options.setCharacterEncoding("UTF-8");
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(2);
        try
        {
            mDoc.save(f, options);
            setDirty(false);
            if (wsFile != null) wsFile.refreshLocal(1, monitor);
        }
        catch (IOException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "Error saving DCCX", e);
        }
        catch (CoreException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "Error after saving DCCX", e);
        }
    }

    @Override
    public void doSaveAs()
    {
        FileDialog dialog = new FileDialog(form.getShell(), SWT.SAVE);
        if (getEditorInput() instanceof FileEditorInput)
        {
            IFile wsFile = ((FileEditorInput) getEditorInput()).getFile();
            dialog.setFilterPath(wsFile.getParent().getRawLocation()
                    .toOSString());
            dialog.setFileName(wsFile.getName());
        }
        String newFile = dialog.open();
        if (newFile != null)
        {
            File f = new File(newFile);
            XmlOptions options = new XmlOptions();
            options.setCharacterEncoding("UTF-8");
            options.setSavePrettyPrint();
            options.setSavePrettyPrintIndent(2);
            try
            {
                mConverter.save(f, options);
            }
            catch (IOException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                        "Error saving DCCX", e);
            }
        }
    }

    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException
    {
        this.setSite(site);
        this.setPartName(input.getName());
        this.setInput(input);
    }

    @Override
    protected void setInput(IEditorInput input)
    {
        super.setInput(input);
        InputStream is = null;
            try
            {
                is = EditorUtils.getInputStream(this);
                
                XmlOptions options = new XmlOptions();
                options.setCharacterEncoding("UTF-8");
                options.setSavePrettyPrint();
                options.setSavePrettyPrintIndent(2);

                mDoc = DocCharConverterDocument.Factory
                        .parse(is, options);
                mConverter = mDoc.getDocCharConverter();
                if (mConverter == null)
                    mConverter = mDoc.addNewDocCharConverter();
            }
            catch (XmlException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                        "Error parsing XML", e);
            }
            catch (IOException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                        "Error reading XML", e);
            }
            catch (CoreException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                        "Core Error while parsing XML", e);
            }
            finally
            {
                try
                {
                    if (is != null)
                        is.close();
                }
                catch (IOException e)
                {
                    DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                            "Error on stream close", e);
                }
                if (mConverter == null)
                {
                    mDoc = DocCharConverterDocument.Factory.newInstance();
                    mConverter = mDoc.addNewDocCharConverter();
                }
            }
        
    }

    @Override
    public boolean isDirty()
    {
        return mDirty;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    @Override
    public void createPartControl(Composite parent)
    {
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getString("DccxEditor"));

        toolkit.createLabel(form.getBody(), MessageUtil
                .getString("ConverterName"));
        final Text name = toolkit.createText(form.getBody(), mConverter
                .getName());
        name.setTextLimit(NAME_LIMIT);
        name.addVerifyListener(new VerifyListener()
        {

            @Override
            public void verifyText(VerifyEvent e)
            {
                if (name.getText().equals(mConverter.getName()) == false)
                {
                    mConverter.setName(name.getText());
                    setDirty(true);
                }
            }
        });
        toolkit.createLabel(form.getBody(), MessageUtil
                .getString("ReverseName"));
        final Text rName = toolkit.createText(form.getBody(), mConverter
                .getRname());
        rName.setTextLimit(NAME_LIMIT);
        rName.addVerifyListener(new VerifyListener()
        {

            @Override
            public void verifyText(VerifyEvent e)
            {
                if (rName.getText().equals(mConverter.getRname()) == false)
                {
                    mConverter.setRname(rName.getText());
                    setDirty(true);
                }
            }
        });
        ColumnLayout layout = new ColumnLayout();
        layout.minNumColumns = 1;
        layout.maxNumColumns = 3;
        form.getBody().setLayout(layout);
        Section converterSection = toolkit.createSection(form.getBody(),
                SWT.LEAD | Section.DESCRIPTION);
        converterSection.setDescription(MessageUtil.getString("Converter"));

        Section parametersSection = toolkit.createSection(form.getBody(),
                SWT.LEAD | Section.DESCRIPTION);
        parametersSection.setDescription(MessageUtil.getString("Parameters"));
        setupConverterParameters(parametersSection);
        setupConverterSection(converterSection);

        Section stylesSection = toolkit.createSection(form.getBody(), SWT.LEAD
                | Section.DESCRIPTION);
        stylesSection.setDescription(MessageUtil.getString("Styles"));
        initStylesTable(stylesSection);

    }

    private void setupConverterParameters(Section parametersSection)
    {
        final Table table = new Table(parametersSection, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        TableColumn typeColumn = new TableColumn(table, SWT.LEAD);
        typeColumn.setText(MessageUtil.getString("Type"));
        typeColumn.setWidth(150);
        TableColumn valueColumn = new TableColumn(table, SWT.LEAD);
        valueColumn.setText(MessageUtil.getString("Value"));
        valueColumn.setWidth(150);
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new IStructuredContentProvider()
        {

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput)
            {

            }

            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof ConverterClass)
                {
                    return ((ConverterClass) inputElement).getArgumentArray();
                }
                return null;
            }
        });
        final ITableLabelProvider labelProvider = new ITableLabelProvider()
        {
            // 
            public Image getColumnImage(Object element, int columnIndex)
            {
                return null;
            }

            public String getColumnText(Object element, int columnIndex)
            {
                if (element instanceof Argument)
                {
                    Argument a = (Argument) element;
                    switch (columnIndex)
                    {
                    case 0:
                        return a.getType().toString();
                    case 1:
                        return a.getValue();
                    }
                }
                return "-";
            }

            @Override
            public void addListener(ILabelProviderListener listener)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void dispose()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isLabelProperty(Object element, String property)
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener)
            {
                // TODO Auto-generated method stub

            }
        };
        viewer.setLabelProvider(labelProvider);
        mParameterViewer = viewer;
        if (mConverter != null && mConverter.getConverterClass() != null
                && mConverter.getConverterClass().sizeOfArgumentArray() > 0)
            viewer.setInput(mConverter.getConverterClass());
        viewer.refresh();
        toolkit.adapt(table);
        parametersSection.setClient(table);

        TableViewerColumn tvc = new TableViewerColumn(viewer, valueColumn);
        final int colNum = 1;
        tvc.setEditingSupport(new EditingSupport(viewer)
        {
            CellEditor tce = null;
            CellEditor fce = null;
            @Override
            protected boolean canEdit(Object element)
            {
                return (element instanceof Argument);
            }

            @Override
            protected CellEditor getCellEditor(Object element)
            {
                Argument arg = (Argument)element;
                if (arg.getType().equals("File"))
                {
                    if (getEditorInput() instanceof FileEditorInput)
                    {
                        FileEditorInput fei = (FileEditorInput)getEditorInput();
                        String value = "";
                        if (arg.isSetValue())
                            value = arg.getValue();
                        IResource fr = fei.getFile().getParent().findMember(value);
                        if (fr instanceof IFile)
                        {
                            IFile file = (IFile)fr;
                            fce = new FileCellEditor(table, SWT.NONE, file, new String[]{"*"});
                            return fce;
                        }
                        else
                        {
                            fce = new FileCellEditor(table, SWT.NONE, fr, new String[]{"*"});
                            return fce;
                        }
                    }
                    
                }
                if (tce == null)
                    tce = new TextCellEditor(table);
                return tce;
            }

            @Override
            protected Object getValue(Object element)
            {
                return labelProvider.getColumnText(element, colNum);
            }

            @Override
            protected void setValue(Object element, Object value)
            {
                String oldValue = "";
                if (getValue(element) != null)
                    oldValue = getValue(element).toString();
                if (value == null || value.toString().equals(oldValue))
                    return;
                try
                {
                    if (element instanceof Argument)
                    {
                        String newValue = value.toString();
                        Argument arg = (Argument)element;
                        if (arg.getType().equals("File"))
                        {
                            FileEditorInput fei = (FileEditorInput)getEditorInput();
                            IContainer parent = fei.getFile().getParent();
                            Path newPath = new Path(newValue);
                            int seg = parent.getRawLocation().matchingFirstSegments(newPath);
                            String up = "";
                            int upCount = parent.getRawLocation().segmentCount() - seg;
                            while (upCount-- > 0)
                                up += "../";
                            IPath relativePath = new Path(up).append(newPath.removeFirstSegments(seg));
                            newValue = relativePath.toPortableString();
                            if (newValue.equals(oldValue))
                                return;
                            arg.setValue(newValue);
                        }
                        else
                            arg.setValue(value.toString());
                    }
                    setDirty(true);
                    viewer.refresh(element);
                }
                catch (NumberFormatException e)
                {

                }
            }

        });
        tvc.setLabelProvider(new CellLabelProvider()
        {

            @Override
            public void update(ViewerCell cell)
            {
                cell.setText(labelProvider.getColumnText(cell.getElement(),
                        cell.getColumnIndex()));
            }
        });

    }

    private void initStylesTable(Section stylesSection)
    {
        Composite stylesComposite = new Composite(stylesSection,
                SWT.SHADOW_ETCHED_IN);
        RowLayout layout = new RowLayout(SWT.VERTICAL);

        stylesComposite.setLayout(layout);

        final Table table = new Table(stylesComposite, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        TableColumn oldColumn = new TableColumn(table, SWT.LEAD);
        oldColumn.setText(MessageUtil.getString("OldFont"));
        oldColumn.setWidth(150);
        TableColumn oldTypeColumn = new TableColumn(table, SWT.LEAD);
        oldTypeColumn.setText(MessageUtil.getString("OldFontType"));
        oldTypeColumn.setWidth(150);
        TableColumn newColumn = new TableColumn(table, SWT.LEAD);
        newColumn.setText(MessageUtil.getString("NewFont"));
        newColumn.setWidth(150);
        TableColumn newTypeColumn = new TableColumn(table, SWT.LEAD);
        newTypeColumn.setText(MessageUtil.getString("NewFontType"));
        newTypeColumn.setWidth(150);
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new IStructuredContentProvider()
        {

            public void dispose()
            {
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput)
            {

            }

            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof Styles)
                {
                    return ((Styles) inputElement).getStyleArray();
                }
                return null;
            }
        });
        final ITableLabelProvider labelProvider = new ITableLabelProvider()
        {
            // ID, Name, Priority, Min
            public Image getColumnImage(Object element, int columnIndex)
            {
                return null;
            }

            public String getColumnText(Object element, int columnIndex)
            {
                if (element instanceof Style)
                {
                    Style s = (Style) element;
                    Age.Enum side = Age.OLD;
                    switch (columnIndex)
                    {
                    case 0:
                    case 1:
                        side = Age.OLD;
                        break;
                    case 3:
                    case 2:
                        side = Age.NEW;
                        break;
                    default:
                        return "";
                    }
                    for (Font f : s.getFontArray())
                    {
                        if (f.getType().equals(side))
                        {
                            String name = "";
                            switch (columnIndex)
                            {
                            case 0:
                            case 2:
                                name = f.getName();
                                break;
                            case 1:
                            case 3:
                                if (f.isSetScript())
                                    name =  MessageUtil.getString("Script_" + f.getScript().toString());
                                break;
                            }
                            if (name == null)
                                name = "";
                            return name;
                        }
                    }
                }
                return "-";
            }

            @Override
            public void addListener(ILabelProviderListener listener)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void dispose()
            {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isLabelProperty(Object element, String property)
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener)
            {
                // TODO Auto-generated method stub

            }
        };
        viewer.setLabelProvider(labelProvider);
        addFontEditorSupport(table, viewer, labelProvider, 0, oldColumn);
        addFontEditorSupport(table, viewer, labelProvider, 1, oldTypeColumn);
        addFontEditorSupport(table, viewer, labelProvider, 2, newColumn);
        addFontEditorSupport(table, viewer, labelProvider, 3, newTypeColumn);
        if (mConverter != null)
            viewer.setInput(mConverter.getStyles());
        viewer.refresh();
        toolkit.adapt(stylesComposite);
        stylesSection.setClient(stylesComposite);
        Button addFontPair = toolkit.createButton(stylesComposite, MessageUtil
                .getString("AddFontPair"), SWT.PUSH);
        Button deleteFontPair = toolkit.createButton(stylesComposite,
                MessageUtil.getString("DeleteFontPair"), SWT.PUSH);
        addFontPair.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (mConverter.getStyles() == null)
                    mConverter.addNewStyles();
                Style s = mConverter.getStyles().addNewStyle();
                s.addNewFont().setType(Age.OLD);
                s.addNewFont().setType(Age.NEW);
                viewer.setInput(mConverter.getStyles());
                viewer.refresh();
                form.reflow(true);
            }
        });
        deleteFontPair.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (mConverter.getStyles() == null)
                    mConverter.addNewStyles();
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                for (int i = 0; i < mConverter.getStyles().sizeOfStyleArray(); i++)
                {
                    Style s = mConverter.getStyles().getStyleArray(i);
                    if (selection.getFirstElement().equals(s))
                    {
                        mConverter.getStyles().removeStyle(i);
                        break;
                    }
                }
                viewer.setInput(mConverter.getStyles());
                viewer.refresh();
                setDirty(true);
            }
        });
    }

    private void addFontEditorSupport(final Table table,
            final TableViewer viewer, final ITableLabelProvider labelProvider,
            int col, TableColumn tc)
    {
        TableViewerColumn tvc = new TableViewerColumn(viewer, tc);
        final int colNum = col;
        tvc.setEditingSupport(new EditingSupport(viewer)
        {
            FontCellEditor tce = null;
            ComboBoxCellEditor typeEditor = null;
            @Override
            protected boolean canEdit(Object element)
            {
                return (element instanceof Style);
            }

            @Override
            protected CellEditor getCellEditor(Object element)
            {
                if (colNum == 1 || colNum == 3)
                {
                    if (typeEditor == null)
                    {
                        String scriptTypes[] = new String[3];
                        scriptTypes[0] = MessageUtil.getString("Script_" + Script.LATIN.toString());
                        scriptTypes[1] = MessageUtil.getString("Script_" + Script.CTL.toString());
                        scriptTypes[2] = MessageUtil.getString("Script_" + Script.CJK.toString());
                        typeEditor = new ComboBoxCellEditor(table, scriptTypes);
                    }
                    return typeEditor;
                }
                if (tce == null)
                    tce = new FontCellEditor(table, SWT.NONE);
                return tce;
            }

            @Override
            protected Object getValue(Object element)
            {
                switch (colNum)
                {
                case 1:
                case 3:
                {
                    if (element instanceof Style)
                    {
                        Style s = (Style)element;
                        Script.Enum script = s.getFontArray((colNum == 1)? 0 : 1).getScript();
                        if (script == Script.LATIN) return new Integer(0);
                        else if (script == Script.CTL) return new Integer(1);
                        else if (script == Script.CJK) return new Integer(2);
                        return new Integer(-1);
                    }
                }
                default:
                    return labelProvider.getColumnText(element, colNum);
                }
            }

            @Override
            protected void setValue(Object element, Object value)
            {
                if (value == null || value.toString().equals(getValue(element)))
                    return;
                try
                {
                    Style style = (Style) element;
                    while (style.sizeOfFontArray() != 2)
                        style.addNewFont();
                    assert (style.sizeOfFontArray() == 2);
                    switch (colNum)
                    {
                    case 0:
                        Font f = style.getFontArray(0);
                        assert (f.getType().equals(Age.OLD));
                        f.setType(Age.OLD);
                        f.setName(value.toString());
                        break;
                    case 1:
                        f = style.getFontArray(0);
                        f.setType(Age.OLD);
                        if (value instanceof Integer)
                        {
                            switch (((Integer)value).intValue())
                            {
                                case 0:
                                    f.setScript(Script.LATIN);
                                    break;
                                case 1:
                                    f.setScript(Script.CTL);
                                    break;
                                case 2:
                                    f.setScript(Script.CJK);
                                    break;
                            }
                        }
                        else f.setScript(Script.Enum.forString(value.toString()));
                        break;
                    case 2:
                        f = style.getFontArray(1);
                        f.setType(Age.NEW);
                        f.setName(value.toString());
                        break;
                    case 3:
                        f = style.getFontArray(1);
                        f.setType(Age.NEW);
                        if (value instanceof Integer)
                        {
                            switch (((Integer)value).intValue())
                            {
                                case 0:
                                    f.setScript(Script.LATIN);
                                    break;
                                case 1:
                                    f.setScript(Script.CTL);
                                    break;
                                case 2:
                                    f.setScript(Script.CJK);
                                    break;
                            }
                        }
                        else f.setScript(Script.Enum.forString(value.toString()));
                        break;
                    }
                    setDirty(true);
                    viewer.refresh(element);
                }
                catch (NumberFormatException e)
                {

                }
            }

        });
        tvc.setLabelProvider(new CellLabelProvider()
        {

            @Override
            public void update(ViewerCell cell)
            {
                cell.setText(labelProvider.getColumnText(cell.getElement(),
                        cell.getColumnIndex()));
            }
        });

    }

    protected void setDirty(boolean b)
    {
        if (mDirty != b)
        {
            mDirty = b;
            firePropertyChange(PROP_DIRTY);
        }
    }

    private void setupConverterSection(Section converterSection)
    {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry
                .getExtensionPoint("org.thanlwinsoft.doccharconvert.converter");
        if (point == null)
            return;
        int selectedIndex = -1;
        String theHelpText = "";
        final Map<String, ConverterProperties> converterMap = new LinkedHashMap<String, ConverterProperties>();
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++)
        {
            IConfigurationElement ce[] = extensions[i]
                    .getConfigurationElements();
            for (int j = 0; j < ce.length; j++)
            {
                if (ce[j].getName().equals(CONVERTER_ELEMENT))
                {
                    String className = ce[j].getAttribute(CLASS_NAME);
                    String plugin = extensions[i].getContributor().getName();
                    Bundle b = Platform.getBundle(plugin);
                    try
                    {
                        Class<?> converterClass = b.loadClass(className);
                        // check it implements the CharConverter interface
                        // boolean isConverter = false;
                        // for (Class<?> iFace : converterClass.getInterfaces())
                        // if (iFace.equals(CharConverter.class))
                        // isConverter = true;
                        // if (!isConverter)
                        // {
                        // DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                        // "Class does not implement CharConverter: "
                        // + converterClass);
                        // break;
                        // }
                        Constructor<?>[] constructors = converterClass
                                .getDeclaredConstructors();
                        int argCount = -1;
                        Constructor<?> best = null;
                        for (Constructor<?> constructor : constructors)
                        {
                            Class<?>[] types = constructor.getParameterTypes();
                            boolean plausible = true;
                            for (Class<?> c : types)
                            {
                                if (!c.equals(String.class)
                                        && !c.equals(URL.class))
                                {
                                    plausible = false;
                                    break;
                                }
                            }
                            if (plausible
                                    && constructor.getParameterTypes().length > argCount)
                            {
                                argCount = constructor.getParameterTypes().length;
                                best = constructor;
                            }
                        }
                        if (best == null)
                            continue;
                        String name = ce[j].getAttribute(NAME);
                        if (name == null)
                            name = className;
                        String helpText = ce[j].getAttribute(HELP);
                        if (mConverter != null
                                && mConverter.getConverterClass() != null
                                && className.equals(mConverter
                                        .getConverterClass().getName()))
                        {
                            selectedIndex = converterMap.size();
                            theHelpText = helpText;
                        }
                        converterMap.put(name, new ConverterProperties(
                                converterClass, best, helpText));
                        // Class[] types = best.getParameterTypes();
                        // for (Class c : types)
                        // {
                        //
                        // }

                    }
                    catch (ClassNotFoundException e)
                    {

                    }
                }
            }
        }
        Composite sectionComposite = new Composite(converterSection, SWT.NONE);
        sectionComposite.setLayout(new GridLayout());
        final Combo combo = new Combo(sectionComposite, SWT.DROP_DOWN);
        final ComboViewer cv = new ComboViewer(combo);
        String[] converterNames = converterMap.keySet().toArray(
                new String[converterMap.size()]);
        cv.setContentProvider(new ArrayContentProvider());
        cv.setInput(converterNames);
        cv.refresh();
        toolkit.adapt(sectionComposite);
        toolkit.adapt(combo);
        converterSection.setClient(sectionComposite);
        
        final Label helpLabel = toolkit.createLabel(sectionComposite,
                theHelpText, SWT.WRAP | SWT.LEAD);
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.widthHint = 300;
        helpLabel.setLayoutData(layoutData);
        combo.addSelectionListener(new SelectionListener()
        {

            @Override
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) cv
                        .getSelection();
                Object o = selection.getFirstElement();
                if (converterMap.containsKey(o))
                {
                    String className = o.toString();
                    ConverterClass cClass = mConverter.getConverterClass();
                    if (cClass == null)
                        cClass = mConverter.addNewConverterClass();
                    ConverterProperties properties = converterMap
                            .get(className);
                    String fullName = properties.clazz.getCanonicalName();
                    helpLabel.setText(properties.helpText);
                    if (!fullName.equals(cClass.getName()))
                    {
                        cClass.setName(fullName);
                        while (cClass.sizeOfArgumentArray() > 0)
                            cClass.removeArgument(0);
                        for (Class<?> param : properties.constructor
                                .getParameterTypes())
                        {
                            Argument arg = cClass.addNewArgument();
                            if (param.equals(String.class))
                                arg.setType(ArgType.STRING);
                            else
                                if (param.equals(File.class)
                                        || param.equals(URL.class))
                                    arg.setType(ArgType.FILE);
                        }
                        mParameterViewer.setInput(cClass);
                        mParameterViewer.refresh();
                        form.reflow(true);
                        setDirty(true);
                    }
                }
            }
        });
        combo.select(selectedIndex);

    }

    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }

    class ConverterProperties
    {
        final Class<?> clazz;
        final Constructor<?> constructor;
        final String helpText;

        public ConverterProperties(Class<?> c, Constructor<?> constructor,
                String help)
        {
            this.clazz = c;
            this.constructor = constructor;
            this.helpText = help;
        }
    }
}
