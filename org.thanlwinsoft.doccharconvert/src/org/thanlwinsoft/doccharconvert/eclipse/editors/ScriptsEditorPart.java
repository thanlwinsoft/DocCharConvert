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
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker;
import org.thanlwinsoft.schemas.syllableParser.Argument;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.Checker;
import org.thanlwinsoft.schemas.syllableParser.Checks;
import org.thanlwinsoft.schemas.syllableParser.Class;
import org.thanlwinsoft.schemas.syllableParser.Columns;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.Side;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.schemas.syllableParser.Type;
import org.w3c.dom.NodeList;

/**
 * @author keith
 * 
 */
public class ScriptsEditorPart extends EditorPart
{
    private final SyllableConverterEditor parentEditor;
    private FormToolkit toolkit;
    private ScrolledForm form;
    
    private final static int COL_WIDTH = 150;

    /**
     * Constructor
     * @param parent
     */
    public ScriptsEditorPart(SyllableConverterEditor parent)
    {
        this.parentEditor = parent;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor)
    {
        parentEditor.doSave(monitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs()
    {
        parentEditor.doSaveAs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException
    {
        this.setSite(site);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty()
    {
        return parentEditor.isDirty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return parentEditor.isSaveAsAllowed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        final SyllableConverter sc = parentEditor.getDocument()
                .getSyllableConverter();
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getString("SyllableParserEditor"));
        // GridLayout layout = new GridLayout();
        // layout.numColumns = 2;
        ColumnLayout layout = new ColumnLayout();
        layout.minNumColumns = 1;
        layout.maxNumColumns = 2;
        form.getBody().setLayout(layout);
        // ColumnLayout layout = new ColumnLayout();
        // form.setLayout(layout);
        Section leftScript = toolkit.createSection(form.getBody(), SWT.LEAD
                | Section.DESCRIPTION);
        Section rightScript = toolkit.createSection(form.getBody(), SWT.LEAD
                | Section.DESCRIPTION);
        // FormText leftName = toolkit.createFormText(form.getBody(), true);
        // leftName.setText(sc.getScriptArray(0).getName(), false, false);
        // FormText rightName = toolkit.createFormText(form.getBody(), true);
        // rightName.setText(sc.getScriptArray(1).getName(), false, false);

        addScriptTable(leftScript, sc.getScriptArray(0));
        addScriptTable(rightScript, sc.getScriptArray(1));
        String leftName = sc.getScriptArray(0).getName();
        String rightName = sc.getScriptArray(1).getName();
        if (leftName == null)
            leftName = "";
        if (rightName == null)
            rightName = "";
        leftScript.setDescription(leftName);
        final Control leftScriptDesc = leftScript.getDescriptionControl();
        if (leftScriptDesc instanceof Text)
        {
            final Text leftScriptNameText = (Text) leftScriptDesc;
            leftScriptNameText.setEditable(true);
            leftScriptNameText.setEnabled(true);
            leftScriptNameText.addModifyListener(new ModifyListener()
            {

                @Override
                public void modifyText(ModifyEvent e)
                {
                    sc.getScriptArray(0).setName(leftScriptNameText.getText());
                    parentEditor.setDirty(true);
                }
            });
        }
        rightScript.setDescription(rightName);
        if (rightScript.getDescriptionControl() instanceof Text)
        {
            final Text rightScriptNameText = (Text) rightScript
                    .getDescriptionControl();
            rightScriptNameText.setEditable(true);
            rightScriptNameText.setEnabled(true);
            rightScriptNameText.addModifyListener(new ModifyListener()
            {

                @Override
                public void modifyText(ModifyEvent e)
                {
                    sc.getScriptArray(1).setName(rightScriptNameText.getText());
                    parentEditor.setDirty(true);
                }
            });
        }

        leftScript.setText(MessageUtil.getString("LeftScript"));
        rightScript.setText(MessageUtil.getString("RightScript"));
        // Section general = toolkit.createSection(form.getBody(), SWT.LEFT);
        // general.setLayout(new ColumnLayout());
        final Button backtrack = toolkit.createButton(form.getBody(),
                MessageUtil.getString("Backtrack"), SWT.CHECK);
        backtrack.setSelection(sc.isSetBacktrack() && sc.getBacktrack());
        backtrack.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                sc.setBacktrack(backtrack.getSelection());
                parentEditor.setDirty(true);
            }
        });
        final Button newClass = toolkit.createButton(form.getBody(),
                MessageUtil.getString("NewClassButton"), SWT.PUSH);
        // Button newClass = new Button(general, SWT.PUSH);
        // newClass.setText(MessageUtil.getString("NewClassButton"));
        newClass.setToolTipText(MessageUtil.getString("NewClassToolTip"));
        newClass.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                NewTableDialog dialog = new NewTableDialog(getEditorSite()
                        .getShell(), getPartName(), null, MessageUtil
                        .getString("CreateNewClassTable"), SWT.SINGLE);
                int open = dialog.open();
                if (open != Window.CANCEL && dialog.name != null
                        && dialog.name.length() > 0
                        && dialog.leftComponents.length > 0
                        && dialog.rightComponents.length > 0)
                {
                    SyllableConverter sc = parentEditor.getDocument()
                            .getSyllableConverter();
                    if (sc.getClasses() == null)
                    {
                        sc.addNewClasses();
                    }
                    org.thanlwinsoft.schemas.syllableParser.Class clazz = sc
                            .getClasses().addNewClass1();
                    clazz.setId(dialog.name);
                    clazz.addNewComponent().setR(dialog.leftComponents[0]);
                    clazz.addNewComponent().setR(dialog.rightComponents[0]);
                    parentEditor.addClassTable(clazz);
                    parentEditor.setDirty(true);
                }
            }
        });
        Button newMapping = toolkit.createButton(form.getBody(), MessageUtil
                .getString("NewMappingButton"), SWT.PUSH);
        // newMapping.setText(MessageUtil.getString("NewMappingButton"));
        newMapping.setToolTipText(MessageUtil.getString("NewMappingToolTip"));
        newMapping.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                NewTableDialog dialog = new NewTableDialog(getEditorSite()
                        .getShell(), getPartName(), null, MessageUtil
                        .getString("CreateNewMappingTable"), SWT.MULTI);
                int open = dialog.open();
                if (open != Window.CANCEL && dialog.name != null
                        && dialog.name.length() > 0
                        && dialog.leftComponents != null
                        && dialog.rightComponents != null
                        && dialog.leftComponents.length > 0
                        && dialog.rightComponents.length > 0)
                {
                    SyllableConverter sc = parentEditor.getDocument()
                            .getSyllableConverter();
                    MappingTable mt = sc.addNewMappingTable();
                    mt.setId(dialog.name);
                    Columns columns = mt.addNewColumns();
                    for (String ref : dialog.leftComponents)
                        columns.addNewComponent().setR(ref);
                    for (String ref : dialog.rightComponents)
                        columns.addNewComponent().setR(ref);
                    parentEditor.addMappingTable(mt);
                    parentEditor.setDirty(true);
                }
            }
        });
        ExpandableComposite syllableCheckers = toolkit
                .createExpandableComposite(form.getBody(), SWT.LEAD
                        | Section.DESCRIPTION);
        syllableCheckers.setText(MessageUtil.getString("SyllableCheckers"));
        addCheckerTable(syllableCheckers);
    }

    private void addCheckerTable(ExpandableComposite parent)
    {
        //findSyllableCheckers();
        final Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        tree.setHeaderVisible(true);
        final TreeViewer viewer = new TreeViewer(tree);
        final ITreeContentProvider contentProvider = new ITreeContentProvider()
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
                if (inputElement instanceof Checks)
                {
                    return ((Checks) inputElement).getCheckerArray();
                }
                return null;
            }

            @Override
            public Object[] getChildren(Object parentElement)
            {
                if (parentElement instanceof Checker)
                {
                    return ((Checker) parentElement).getArgArray();
                }
                return null;
            }

            @Override
            public Object getParent(Object element)
            {
                Checks checks = parentEditor.getDocument()
                        .getSyllableConverter().getChecks();
                if (element instanceof Checker)
                {
                    return checks;
                }
                else
                    if (element instanceof Argument)
                    {
                        for (Checker c : checks.getCheckerArray())
                        {
                            for (Argument a : c.getArgArray())
                            {
                                if (a == element)
                                    return c;
                            }
                        }
                    }
                return null;
            }

            @Override
            public boolean hasChildren(Object element)
            {
                if (element instanceof Argument)
                    return false;
                else
                    if (element instanceof Checker
                            && ((Checker) element).sizeOfArgArray() > 0)
                        return true;
                    else
                        if (element instanceof Checks
                                && ((Checks) element).sizeOfCheckerArray() > 0)
                            return true;
                return false;
            }
        };

        viewer.setContentProvider(contentProvider);
        
        TreeColumn columnA = new TreeColumn(tree, SWT.LEAD);
        columnA.setText(MessageUtil.getString("Type"));
        columnA.setWidth(COL_WIDTH);
        TreeColumn columnB = new TreeColumn(tree, SWT.LEAD);
        columnB.setText(MessageUtil.getString("Value"));
        columnB.setWidth(COL_WIDTH);
        TreeViewerColumn colAViewer = new TreeViewerColumn(viewer, columnA);
        TreeViewerColumn colBViewer = new TreeViewerColumn(viewer, columnB);
        colAViewer.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element)
            {
                if (element instanceof Checker)
                {
                    Checker c = (Checker)element;
                    if (parentEditor.getCheckerNameMap().containsKey(c.getClass1()))
                        return parentEditor.getCheckerNameMap().get(c.getClass1());
                    return c.getClass1();
                }
                if (element instanceof Argument)
                {
                    Checks checks = parentEditor.getDocument().getSyllableConverter().getChecks();
                    for (Checker c : checks.getCheckerArray())
                    {
                        for (int i = 0; i < c.sizeOfArgArray(); i++)
                        {
                            if (c.getArgArray(i) == element)
                            {
                                if (parentEditor.getCheckerMap().containsKey(c.getClass1()))
                                {
                                    SyllableChecker sc = parentEditor.getCheckerMap().get(c.getClass1());
                                    String [] argDesc = sc.getArgumentDescriptions();
                                    if (argDesc.length > i)
                                        return argDesc[i];
                                    return Integer.toString(i);
                                }
                            }
                        }
                    }
                }
                return super.getText(element);
            }
            
        });
        colBViewer.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element)
            {
                if (element instanceof Argument)
                {
                    Argument arg = (Argument)element;
                    StringBuilder value = new StringBuilder();
                    NodeList textNodes = arg.getDomNode().getChildNodes();
                    for (int i = 0; i < textNodes.getLength(); i++)
                    {
                        value.append(textNodes.item(i).getNodeValue());
                    }
                    
                    return value.toString();
                }
                return "";
            }
        });
        colBViewer.setEditingSupport(new EditingSupport(viewer){
            private TextCellEditor textEditor = new TextCellEditor(tree);
            @Override
            protected boolean canEdit(Object element)
            {
                if (element instanceof Argument)
                    return true;
                return false;
            }

            @Override
            protected CellEditor getCellEditor(Object element)
            {
                
                return textEditor;
            }

            @Override
            protected Object getValue(Object element)
            {
                if (element instanceof Argument)
                {
                    Argument arg = (Argument)element;
                    StringBuilder value = new StringBuilder();
                    NodeList textNodes = arg.getDomNode().getChildNodes();
                    for (int i = 0; i < textNodes.getLength(); i++)
                    {
                        value.append(textNodes.item(i).getNodeValue());
                    }
                    
                    return value.toString();
                }
                return "";
            }

            @Override
            protected void setValue(Object element, Object value)
            {
                String newValue = value.toString();
                Argument arg = (Argument)element;
                while (arg.getDomNode().hasChildNodes())
                    arg.getDomNode().removeChild(arg.getDomNode().getLastChild());
                org.w3c.dom.Text textNode = arg.getDomNode().getOwnerDocument().createTextNode(newValue);
                arg.getDomNode().appendChild(textNode);
                viewer.refresh();
                parentEditor.setDirty(true);
            }});
        viewer.setInput(parentEditor.getDocument().getSyllableConverter()
                .getChecks());
        
        MenuManager menuManager = new MenuManager(parentEditor.getPartName()
                + ":" + this.getPartName() + "Checkers");
        for (String className : parentEditor.getCheckerNameMap().keySet())
        {
            final String clazzName = className;
            Action newChecker = new Action(){
                @Override
                public void run()
                {
                    Checks checks = parentEditor.getDocument().getSyllableConverter().getChecks();
                    if (checks == null)
                        checks = parentEditor.getDocument().getSyllableConverter().addNewChecks();
                    Checker checker = checks.addNewChecker();
                    checker.setClass1(clazzName);
                    SyllableChecker theChecker = parentEditor.getCheckerMap().get(clazzName);
                    for (java.lang.Class<?> argType : theChecker.getArgumentTypes())
                    {
                        if (argType.equals(File.class) || argType.equals(URL.class))
                        {
                            checker.addNewArg().setType(Type.FILE);
                        }
                        else
                        {
                            checker.addNewArg();
                        }
                    }
                    viewer.setInput(checks);
                    viewer.refresh();
                    parentEditor.setDirty(true);
                    form.reflow(true);
                }
                
            };
            newChecker.setText(MessageUtil.getString("AddChecker",
                    parentEditor.getCheckerNameMap().get(className)));
            menuManager.add(newChecker);
        }
        Action deleteChecker = new Action()
        {

            @Override
            public void run()
            {
                Checks checks = parentEditor.getDocument().getSyllableConverter().getChecks();
                
                ITreeSelection selection = (ITreeSelection)viewer.getSelection();
                Object s = selection.getFirstElement();
                if (s instanceof Argument)
                {
                    s = contentProvider.getParent(s);
                }
                if (s instanceof Checker)
                {
                    Checker c = (Checker)s;
                    for (int i = 0; i < checks.sizeOfCheckerArray(); i++)
                    {
                        if (checks.getCheckerArray(i) == c)
                        {
                            checks.removeChecker(i);
                            viewer.refresh();
                            parentEditor.setDirty(true);
                            break;
                        }
                    }
                }
            }
        };
        deleteChecker.setText(MessageUtil.getString("DeleteChecker"));
        menuManager.add(deleteChecker);
        
        Action moveUpAction = new Action()
        {
            public void run()
            {
                Checks checks = parentEditor.getDocument().getSyllableConverter().getChecks();
                ITreeSelection selection = (ITreeSelection)viewer.getSelection();
                Object s = selection.getFirstElement();
                if (s instanceof Argument)
                {
                    s = contentProvider.getParent(s);
                }
                if (s instanceof Checker)
                {
                    Checker c = (Checker)s;
                    int mapIndex = -1;
                    for (int i = 0; i < checks.sizeOfCheckerArray(); i++)
                    {
                        if (checks.getCheckerArray(i) == c)
                            mapIndex = i;
                    }
                    if (mapIndex > 0)
                    {
                        XmlObject toMove = checks.getCheckerArray(mapIndex).copy();
                        checks.removeChecker(mapIndex);
                        Checker moved = checks.insertNewChecker(mapIndex - 1);
                        moved.set(toMove);
                        viewer.refresh();
                        parentEditor.setDirty(true);
                    }
                }
            }
        };
        moveUpAction.setId("moveUp");
        moveUpAction.setText(MessageUtil.getString("MoveUp"));
        moveUpAction.setToolTipText(MessageUtil.getString("MoveUpToolTip"));

        Action moveDownAction = new Action()
        {
            public void run()
            {
                Checks checks = parentEditor.getDocument().getSyllableConverter().getChecks();
                ITreeSelection selection = (ITreeSelection)viewer.getSelection();
                Object s = selection.getFirstElement();
                if (s instanceof Argument)
                {
                    s = contentProvider.getParent(s);
                }
                if (s instanceof Checker)
                {
                    Checker c = (Checker)s;
                    int mapIndex = -1;
                    for (int i = 0; i < checks.sizeOfCheckerArray(); i++)
                    {
                        if (checks.getCheckerArray(i) == c)
                            mapIndex = i;
                    }
                    if (mapIndex > -1 && mapIndex + 1 < checks.sizeOfCheckerArray())
                    {
                        XmlObject toMove = checks.getCheckerArray(mapIndex).copy();
                        checks.removeChecker(mapIndex);
                        Checker moved = checks.insertNewChecker(mapIndex + 1);
                        moved.set(toMove);
                    
                        viewer.refresh();
                        parentEditor.setDirty(true);
                    }
                }
            }
        };
        moveDownAction.setId("moveDown");
        moveDownAction.setText(MessageUtil.getString("MoveDown"));
        moveDownAction.setToolTipText(MessageUtil.getString("MoveDownToolTip"));

        menuManager.add(moveUpAction);
        menuManager.add(moveDownAction);
        
        toolkit.adapt(tree);
        tree.setMenu(menuManager.createContextMenu(tree));
        parent.setClient(tree);
        viewer.refresh();
    }

    /**
     * @param section
     * @param scriptArray
     */
    private void addScriptTable(ExpandableComposite parent,
            final Script scriptArray)
    {
        final Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
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
                if (inputElement instanceof Script)
                {
                    return ((Script) inputElement).getCluster()
                            .getComponentArray();
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
                if (element instanceof Component)
                {
                    Component c = (Component) element;
                    switch (columnIndex)
                    {
                    case 0:
                        return c.getId();
                    case 1:
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < c.getDomNode().getChildNodes()
                                .getLength(); i++)
                            builder.append(c.getDomNode().getChildNodes().item(
                                    i).getNodeValue());
                        return builder.toString();
                    case 2:
                        if (c.isSetPriority())
                            return c.getPriority().toString();
                        else
                            return "";
                    case 3:
                        if (c.isSetMin())
                            return c.getMin().toString();
                        else
                            return "";
                    }
                }
                return "";
            }

            public void addListener(ILabelProviderListener listener)
            {
            }

            public void dispose()
            {
            }

            public boolean isLabelProperty(Object element, String property)
            {
                return false;
            }

            public void removeListener(ILabelProviderListener listener)
            {
            }
        };
        viewer.setLabelProvider(labelProvider);
        viewer.setInput(scriptArray);
        String[] columnNames = new String[] { MessageUtil.getString("ID"),
                MessageUtil.getString("Name"),
                MessageUtil.getString("Priority"),
                MessageUtil.getString("Minimum"), };
        for (int i = 0; i < 4; i++)
        {
            TableColumn col = new TableColumn(table, SWT.LEAD);
            col.setWidth(100);
            col.setText(columnNames[i]);
            TableViewerColumn tvc = new TableViewerColumn(viewer, col);
            final int colNum = i;
            tvc.setEditingSupport(new EditingSupport(viewer)
            {
                TextCellEditor tce = null;

                @Override
                protected boolean canEdit(Object element)
                {
                    return (element instanceof Component);
                }

                @Override
                protected CellEditor getCellEditor(Object element)
                {
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
                    if (value == null
                            || value.toString().equals(getValue(element)))
                        return;
                    try
                    {
                        Component c = (Component) element;
                        switch (colNum)
                        {
                        case 0:
                            if (c.getId() == null || !c.getId().equals(value.toString()))
                            {
                                if (isUniqueComponentId(value.toString()))
                                {
                                    switchComponentId(c.getId(), value.toString());
                                    c.setId(value.toString());
                                }
                            }
                            break;
                        case 1:
                            org.w3c.dom.Text t = c.getDomNode()
                                    .getOwnerDocument().createTextNode(
                                            value.toString());
                            while (c.getDomNode().hasChildNodes())
                                c.getDomNode().removeChild(c.getDomNode().getFirstChild());
                            c.getDomNode().appendChild(t);
                            break;
                        case 2:
                            int iValue = Integer.parseInt(value.toString());
                            c.setPriority(BigInteger.valueOf(iValue));
                            break;
                        case 3:
                            int minValue = Integer.parseInt(value.toString());
                            c.setMin(BigInteger.valueOf(minValue));
                            break;
                        }
                        parentEditor.setDirty(true);
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
        MenuManager menuManager = new MenuManager(parentEditor.getPartName()
                + ":" + this.getPartName());
        Action insertAction = new Action()
        {
            public void run()
            {
                int mapIndex = table.getSelectionIndex();// redo if sort
                if (mapIndex < 0)
                    mapIndex = scriptArray.getCluster().sizeOfComponentArray();
                int insertRowCount = Math.max(1, table.getSelectionCount());
                for (int i = 0; i < insertRowCount; i++)
                {
                    Component c;
                    if (mapIndex + i < scriptArray.getCluster().sizeOfComponentArray())
                        c = scriptArray.getCluster()
                            .insertNewComponent(mapIndex + i);
                    else c= scriptArray.getCluster().addNewComponent();
                    int numComponents = scriptArray.getCluster()
                            .sizeOfComponentArray();
                    String id = "Side" + scriptArray.getSide() + numComponents;
                    if (scriptArray.getSide().equals(Side.LEFT))
                    {
                        id = MessageUtil.getString("LeftComponent", Integer
                                .toString(numComponents));
                        for (int j = 0; j < scriptArray.getCluster()
                                .sizeOfComponentArray(); j++)
                        {
                            Component existing = scriptArray.getCluster()
                                    .getComponentArray(j);
                            if (existing.getId() != null
                                    && existing.getId().equals(id))
                            {
                                id = MessageUtil.getString("LeftComponent",
                                        Integer.toString(++numComponents));
                                j = 0;
                            }
                        }
                    }
                    else
                        if (scriptArray.getSide().equals(Side.RIGHT))
                        {
                            id = MessageUtil.getString("RightComponent",
                                    Integer.toString(numComponents));
                            for (int j = 0; j < scriptArray.getCluster()
                                    .sizeOfComponentArray(); j++)
                            {
                                Component existing = scriptArray.getCluster()
                                        .getComponentArray(j);
                                if (existing.getId() != null
                                        && existing.getId().equals(id))
                                {
                                    id = MessageUtil.getString(
                                            "RightComponent", Integer
                                                    .toString(++numComponents));
                                    j = 0;
                                }
                            }
                        }
                    c.setId(id);
                }
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        insertAction.setId("Insert");
        insertAction.setText(MessageUtil.getString("Insert"));
        insertAction.setToolTipText(MessageUtil.getString("InsertToolTip"));
        Action deleteAction = new Action()
        {
            public void run()
            {
                int mapIndex = table.getSelectionIndex();
                if (mapIndex < 0)
                    return;
                scriptArray.getCluster().removeComponent(mapIndex);

                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        deleteAction.setId("Delete");
        deleteAction.setText(MessageUtil.getString("Delete"));
        deleteAction.setToolTipText(MessageUtil.getString("DeleteToolTip"));
        Action moveUpAction = new Action()
        {
            public void run()
            {
                int mapIndex = table.getSelectionIndex();
                if (mapIndex < 1)
                    return;
                XmlObject toMove = scriptArray.getCluster().getComponentArray(
                        mapIndex).copy();
                scriptArray.getCluster().removeComponent(mapIndex);
                Component moved = scriptArray.getCluster().insertNewComponent(
                        mapIndex - 1);
                moved.set(toMove);
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        moveUpAction.setId("moveUp");
        moveUpAction.setText(MessageUtil.getString("MoveUp"));
        moveUpAction.setToolTipText(MessageUtil.getString("MoveUpToolTip"));

        Action moveDownAction = new Action()
        {
            public void run()
            {
                int mapIndex = table.getSelectionIndex();
                int numComponents = scriptArray.getCluster()
                        .sizeOfComponentArray();
                if (mapIndex < 0 || mapIndex == numComponents - 1)
                    return;
                XmlObject toMove = scriptArray.getCluster().getComponentArray(
                        mapIndex).copy();
                scriptArray.getCluster().removeComponent(mapIndex);
                Component moved = scriptArray.getCluster().insertNewComponent(
                        mapIndex + 1);
                moved.set(toMove);
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        moveDownAction.setId("moveDown");
        moveDownAction.setText(MessageUtil.getString("MoveDown"));
        moveDownAction.setToolTipText(MessageUtil.getString("MoveDownToolTip"));

        menuManager.add(insertAction);
        menuManager.add(deleteAction);
        menuManager.add(moveUpAction);
        menuManager.add(moveDownAction);
        menuManager.add(new Separator());
        menuManager.setVisible(true);
        toolkit.adapt(table);
        table.setMenu(menuManager.createContextMenu(table));
        parent.setClient(table);
        viewer.refresh();

    }

    /**
     * @param string
     * @return
     */
    protected boolean isUniqueComponentId(String string)
    {
        if (string == null || string.length() == 0) return false;
        SyllableConverter sc = parentEditor.getDocument().getSyllableConverter(); 
        for (Script s : sc.getScriptArray())
        {
            for (Component c : s.getCluster().getComponentArray())
            {
                if (c.getId() != null && c.getId().equals(string))
                    return false;
            }
        }
        return true;
    }

    /**
     * @param id
     * @param string
     */
    protected void switchComponentId(String oldId, String newId)
    {
        if (oldId == null || oldId.length() == 0) return;
        SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
        // parse the classes
        for (Class clazz : sc.getClasses().getClass1Array())
        {
            for (ComponentRef comp : clazz.getComponentArray())
            {
                if (comp.getR() != null && comp.getR().equals(oldId))
                        comp.setR(newId);
            }
        }
        // parse the maps
        for (MappingTable mt : sc.getMappingTableArray())
        {
            for (ComponentRef cols : mt.getColumns().getComponentArray())
            {
                if (cols.getR() != null && cols.getR().equals(oldId))
                {
                    cols.setR(newId);
                }
            }
            for (org.thanlwinsoft.schemas.syllableParser.Map m : mt.getMaps().getMArray())
            {
                for (C c : m.getCArray())
                {
                    if (c.getR() != null && c.getR().equals(oldId))
                    {
                        c.setR(newId);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        form.setFocus();
    }

    /**
     * Dialog to create a new Mapping Table editor part
     * @author keith
     *
     */
    public class NewTableDialog extends MessageDialog
    {

        private int style = SWT.NONE;

        /**
         * @param parentShell
         * @param dialogTitle
         * @param dialogTitleImage
         * @param dialogMessage
         * @param style 
         * @param dialogImageType
         * @param dialogButtonLabels
         * @param defaultIndex
         */
        public NewTableDialog(Shell parentShell, String dialogTitle,
                Image dialogTitleImage, String dialogMessage, int style)
        {
            super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
                    MessageDialog.INFORMATION, new String[] {
                            MessageUtil.getString(MessageUtil.getString("OK")),
                            MessageUtil.getString("Cancel") }, 0);
            this.style = style;
        }

        String name;
        String[] leftComponents;
        String[] rightComponents;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createCustomArea(Composite parent)
        {
            Composite control = new Composite(parent, SWT.LEAD);
            GridLayout gl = new GridLayout();
            gl.numColumns = 4;
            control.setLayout(gl);
            Label l1 = new Label(control, SWT.LEAD);
            l1.setText(MessageUtil.getString("Name"));
            final Text nameText = new Text(control, SWT.LEAD);
            nameText.addModifyListener(new ModifyListener()
            {

                public void modifyText(ModifyEvent e)
                {
                    name = nameText.getText();
                }
            });
            GridData nameGD = new GridData();
            nameGD.grabExcessHorizontalSpace = true;
            nameGD.horizontalSpan = 3;
            nameText.setLayoutData(nameGD);
            Label lLeft = new Label(control, SWT.LEAD);
            lLeft.setText(MessageUtil.getString("LeftComponents"));
            SyllableConverter sc = parentEditor.getDocument()
                    .getSyllableConverter();
            final List leftList = new List(control, style);
            ListViewer leftViewer = new ListViewer(leftList);
            leftViewer.setLabelProvider(new LabelProvider());
            ArrayList<String> leftComponentsArray = new ArrayList<String>();
            for (Component c : sc.getScriptArray(0).getCluster()
                    .getComponentArray())
            {
                leftComponentsArray.add(c.getId());
            }
            leftViewer.add(leftComponentsArray.toArray());
            leftList.addSelectionListener(new SelectionListener()
            {
                public void widgetDefaultSelected(SelectionEvent e)
                {
                }

                public void widgetSelected(SelectionEvent e)
                {
                    leftComponents = leftList.getSelection();
                }
            });
            Label lRight = new Label(control, SWT.LEAD);
            lRight.setText(MessageUtil.getString("RightComponents"));
            final List rightList = new List(control, style);
            ListViewer rightViewer = new ListViewer(rightList);
            rightViewer.setLabelProvider(new LabelProvider());
            ArrayList<String> rightComponentsArray = new ArrayList<String>();
            for (Component c : sc.getScriptArray(1).getCluster()
                    .getComponentArray())
            {
                rightComponentsArray.add(c.getId());
            }
            rightViewer.add(rightComponentsArray.toArray());
            rightList.addSelectionListener(new SelectionListener()
            {
                public void widgetDefaultSelected(SelectionEvent e)
                {
                }

                public void widgetSelected(SelectionEvent e)
                {
                    rightComponents = rightList.getSelection();
                }
            });
            return control;
        }

    }
    
}
