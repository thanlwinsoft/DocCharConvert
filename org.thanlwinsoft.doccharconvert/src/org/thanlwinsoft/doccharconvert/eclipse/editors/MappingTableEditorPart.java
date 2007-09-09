/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Iterator;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.schemas.syllableParser.Maps;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.Map;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverterDocument;



/**
 * @author keith
 *
 */
public class MappingTableEditorPart extends EditorPart
{
    private MappingTable mt = null;
    final private SyllableConverterEditor parentEditor; 
    private Table table = null;
    private TableViewer viewer = null;
    private MenuManager menuManager;
    private Clipboard clipboard;
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
        if (clipboard != null && !clipboard.isDisposed())
        {
            clipboard.clearContents();
            clipboard.dispose();
        }
        super.dispose();
    }
    MappingTableEditorPart(SyllableConverterEditor parentEditor, MappingTable mt)
    {
        this.mt = mt;
        this.setPartName(mt.getId());
        this.setContentDescription(mt.getId());
        this.parentEditor = parentEditor;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor)
    {
        parentEditor.doSave(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs()
    {
        parentEditor.doSaveAs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        this.setSite(site);
        clipboard = new Clipboard(site.getShell().getDisplay());
        menuManager = new MenuManager(parentEditor.getPartName() + ":" + mt.getId());
        Action insertAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0)
                    mapIndex = mt.getMaps().sizeOfMArray();
                int insertRowCount =  Math.max(1, table.getSelectionCount());
                for (int i = 0; i < insertRowCount; i++)
                    mt.getMaps().insertNewM(mapIndex);
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        insertAction.setId("Insert");
        insertAction.setText(MessageUtil.getString("InsertRow"));
        insertAction.setToolTipText(MessageUtil.getString("InsertRowToolTip"));
        Action deleteAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0)
                    return;
                mt.getMaps().removeM(mapIndex);
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        deleteAction.setId("Delete");
        deleteAction.setText(MessageUtil.getString("DeleteRow"));
        deleteAction.setToolTipText(MessageUtil.getString("DeleteRowToolTip"));
        
        final IEditorPart part = this; 
        Action deleteTableAction = new Action(){
            public void run()
            {
                if (MessageDialog.openConfirm(part.getSite().getShell(), 
                    part.getTitle(), 
                    MessageUtil.getString("ConfirmDeleteTable"))==false)
                {
                    return;
                }
                int index = parentEditor.getEditorIndex(part);
                parentEditor.removePage(index);
                SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                for (int i = 0; i < sc.sizeOfMappingTableArray(); i++)
                {
                    if (sc.getMappingTableArray(i) == mt)
                    {
                        sc.removeMappingTable(i);
                        break;
                    }
                }
                parentEditor.setDirty(true);
            }
        };
        deleteTableAction.setId("DeleteTable");
        deleteTableAction.setText(MessageUtil.getString("DeleteTable"));
        deleteTableAction.setToolTipText(MessageUtil.getString("DeleteTableToolTip"));
        menuManager.add(deleteTableAction);
        menuManager.add(insertAction);
        menuManager.add(deleteAction);
        
        menuManager.add(new Separator());
        Action optionalAction = new Action(MessageUtil.getString("OptionalTable"), 
            Action.AS_CHECK_BOX)
        {
            public void run()
            {
                mt.setOptional(this.isChecked());
            }
        };
        optionalAction.setText(MessageUtil.getString("OptionalTable"));
        optionalAction.setToolTipText(MessageUtil.getString("OptionalTableToolTip"));
        if (mt.isSetOptional() && mt.getOptional())
        {
            optionalAction.setChecked(true);
        }
        else
        {
            optionalAction.setChecked(false);
        }
        menuManager.add(optionalAction);
        
        menuManager.add(new Separator());
        
        SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
        int usedIndex = 0;
        MenuManager addColumns = new MenuManager(MessageUtil.getString("AddColumn"));
        MenuManager copyColumns = new MenuManager(MessageUtil.getString("Copy"));
        MenuManager cutColumns = new MenuManager(MessageUtil.getString("Cut"));
        MenuManager pasteColumns = new MenuManager(MessageUtil.getString("Paste"));
        addColumns.setParent(menuManager);
        menuManager.add(addColumns);
        copyColumns.setParent(menuManager);
        menuManager.add(copyColumns);
        cutColumns.setParent(menuManager);
        menuManager.add(cutColumns);
        pasteColumns.setParent(menuManager);
        menuManager.add(pasteColumns);
        
        Action copyAction = new Action()
        {
            public void run()
            {
                doCopy(false);
            }
        };
        copyAction.setText(MessageUtil.getString("Copy"));
        menuManager.add(copyAction);
        Action cutAction = new Action()
        {
            public void run()
            {
                doCopy(true);
            }
        };
        cutAction.setText(MessageUtil.getString("Cut"));
        menuManager.add(cutAction);
        Action pasteAction = new Action()
        {
            public void run()
            {
                doPaste();
            }
        };
        pasteAction.setText(MessageUtil.getString("Paste"));
        menuManager.add(pasteAction);
        
        for (Script script : sc.getScriptArray())
        {
            menuManager.add(new Separator());
            for (Component c : script.getCluster().getComponentArray())
            {
                boolean used = false;
                for (ComponentRef cr : mt.getColumns().getComponentArray())
                {
                    if (cr.getR().equals(c.getId()))
                    {
                        used = true; 
                        ++usedIndex;
                        break;
                    }
                }
                final String cId = c.getId();
                final int insertPos = usedIndex;
                Action addColumn = new Action()
                {
                    public void run()
                    {
                        if (this.isChecked())
                        {
                            ComponentRef ncr = mt.getColumns().insertNewComponent(insertPos);
                            ncr.setR(cId);
                        }
                        else
                        {
                            for (int i = 0; i < mt.getColumns().sizeOfComponentArray(); i++)
                            {
                                if (mt.getColumns().getComponentArray(i).getR().equals(cId))
                                {
                                    mt.getColumns().removeComponent(i);
                                    break;
                                }
                            }
                        }
                        //viewer.refresh();
                        try
                        {
                            int index = parentEditor.getEditorIndex(part);
                            IEditorPart replacement = 
                                new MappingTableEditorPart(parentEditor, mt); 

                            parentEditor.addPage(index + 1, replacement,  parentEditor.getEditorInput());
                            parentEditor.setDirty(true);
                            parentEditor.removePage(index);
                            parentEditor.setActiveEditor(replacement);
                        }
                        catch (PartInitException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };
                addColumn.setText(c.getId());
                addColumn.setChecked(used);
                addColumns.add(addColumn);
                if (!used) continue;
                Action copyColumn = new Action()
                {
                    public void run() { doCopy(cId, false); }
                };
                copyColumn.setText(c.getId());
                copyColumns.add(copyColumn);
                Action cutColumn = new Action()
                {
                    public void run() { doCopy(cId, true); }
                };
                cutColumn.setText(c.getId());
                cutColumns.add(cutColumn);
                Action pasteColumn = new Action()
                {
                    public void run() { doPaste(cId); }
                };
                pasteColumn.setText(c.getId());
                pasteColumns.add(pasteColumn);
                
            }
        }
        //SyllableConverterDocument doc = this.parentEditor.getDocument();
        // TODO check table hasn't changed
    }
    
    protected void doCopy(boolean cut)
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return;
        IStructuredSelection ss = (IStructuredSelection)viewer.getSelection();
        Iterator <?>i = ss.iterator();
        Maps data = Maps.Factory.newInstance();
        while (i.hasNext())
        {
            Object o = i.next();
            if (o instanceof Map)
            {
                Map copy = data.addNewM();
                copy.set((Map)o);
                if (cut)
                {
                    Map m = (Map)o; 
                    while (m.sizeOfCArray() > 0)
                    {
                        m.removeC(0);
                    }
                }
            }
        }
        String xmlData = data.xmlText();
        clipboard.setContents(new Object [] {xmlData}, new Transfer[] { TextTransfer.getInstance()});
        if (cut)
        {
            parentEditor.setDirty(true);
            viewer.refresh(ss.toList().toArray());
        }
    }
    
    protected void doCopy(String ref, boolean cut)
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return;
        IStructuredSelection ss = (IStructuredSelection)viewer.getSelection();
        Iterator <?>i = ss.iterator();
        Map data = Map.Factory.newInstance();
        while (i.hasNext())
        {
            Object o = i.next();
            if (o instanceof Map)
            {
                C c = SyllableConverterUtils.getCFromMap((Map)o, ref);
                C copy = data.addNewC();
                copy.set(c);
                if (cut)
                {
                    c.unsetHex();
                    c.unsetClass1();
                    c.setStringValue("");
                    viewer.refresh(o);
                }
            }
        }
        String xmlData = data.xmlText();
        clipboard.setContents(new Object [] {xmlData}, new Transfer[] { TextTransfer.getInstance()});
        if (cut)
        {
            parentEditor.setDirty(true);
            //viewer.refresh(ss.toList().toArray());
        }
        
    }
    
    protected void doPaste()
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return;
        IStructuredSelection ss = (IStructuredSelection)viewer.getSelection();
        Iterator <?>i = ss.iterator();
        String data = (String)clipboard.getContents(TextTransfer.getInstance());
        if (data == null) return;
        try
        {
            Maps maps = Maps.Factory.parse(data);
            int iData = 0;
            while (i.hasNext() && iData < maps.sizeOfMArray())
            {
                Object o = i.next();
                if (o instanceof Map)
                {
                    Map m = (Map)o;
                    m.set(maps.getMArray(iData));
                    viewer.refresh(o);
                    // if there is only one item do a fill down, otherwise
                    // copy sequentially
                    if (maps.sizeOfMArray() > 1)
                        iData++;
                }
            }
            parentEditor.setDirty(true);
            //viewer.refresh();
        }
        catch (XmlException e)
        {
            // invalid xml
        }
    }
    
    protected void doPaste(String ref)
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return;
        IStructuredSelection ss = (IStructuredSelection)viewer.getSelection();
        Iterator <?>i = ss.iterator();
        String data = (String)clipboard.getContents(TextTransfer.getInstance());
        if (data == null) return;
        try
        {
            Map m = Map.Factory.parse(data);
            int iData = 0;
            while (i.hasNext() && iData < m.sizeOfCArray())
            {
                Object o = i.next();
                if (o instanceof Map)
                {
                    C c = SyllableConverterUtils.getCFromMap((Map)o, ref);
                    C newC = m.getCArray(iData);
                    if (c == null)
                        c = ((Map)o).addNewC();
                    c.set(newC);
                    // restore the ref
                    c.setR(ref);
                    viewer.refresh(o);
                    // if there is only one item do a fill down, otherwise
                    // copy sequentially
                    if (m.sizeOfCArray() > 1)
                        iData++;
                }
            }
            parentEditor.setDirty(true);
            //viewer.refresh();
        }
        catch (XmlException e)
        {
            // invalid xml
        }
    }
    
    protected int getSelectedMapIndex()
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return -1;
        IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
        if (s.getFirstElement() instanceof Map)
        {
            Map selectedMap = (Map)s.getFirstElement();
            for (int i = 0; i < mt.getMaps().sizeOfMArray(); i++)
            {
                if (mt.getMaps().getMArray(i) == selectedMap)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty()
    {
        return parentEditor.isDirty();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return parentEditor.isSaveAsAllowed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        parent.setLayout(new FillLayout());
        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        viewer = new TableViewer(table);
        viewer.setContentProvider(new MappingTableContentProvider());
        MappingTableLabelProvider mtlp = new MappingTableLabelProvider(mt); 
        viewer.setLabelProvider(mtlp);
        for (ComponentRef cr : mt.getColumns().getComponentArray())
        {
            final String colRef = cr.getR();
            TableColumn tc = new TableColumn(table, SWT.LEAD);
            tc.setText(cr.getR());
            tc.setWidth(70);
            TableViewerColumn tvc = new TableViewerColumn(viewer, tc);
            SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
            tc.setToolTipText(SyllableConverterUtils.getComponentName(sc, colRef));
            tvc.setEditingSupport(new CellEditingSupport(viewer, colRef));
            tvc.setLabelProvider(new CellLabelProvider(){
                
                @Override
                public void update(ViewerCell cell)
                {
                    Object o = cell.getViewerRow().getElement();
                    if (o instanceof Map)
                    {
                        for (C c : ((Map)o).getCArray())
                        {
                            if (c.getR().equals(colRef))
                            {
                                cell.setText(SyllableConverterUtils.getCText(c));
                                c.getR();
                            }
                        }
                    }
                }});
        }
        viewer.setInput(mt);
        table.setHeaderVisible(true);
        viewer.refresh();
        menuManager.add(new GroupMarker (IWorkbenchActionConstants.MB_ADDITIONS));
        this.getEditorSite().registerContextMenu(menuManager, viewer);
        this.getEditorSite().setSelectionProvider(viewer);
        menuManager.setVisible(true);
        table.setMenu(menuManager.createContextMenu(table));
        table.setToolTipText(MessageUtil.getString("MappingTableToolTip"));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        
    }
    
    public class CellEditingSupport extends EditingSupport
    {
        final String colRef;
        CellEditor editor;
        /**
         * @param viewer
         */
        public CellEditingSupport(ColumnViewer viewer, String colRef)
        {
            super(viewer);
            this.colRef = colRef;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
         */
        @Override
        protected boolean canEdit(Object element)
        {
            if (element instanceof Map)
            {
                return true;
            }
            return false;
        }

        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
         */
        @Override
        protected CellEditor getCellEditor(Object element)
        {
            editor = null;
            Composite composite = (Composite)this.getViewer().getControl();
            if (element instanceof Map)
            {
                Map m = (Map)element;
                C c = SyllableConverterUtils.getCFromMap(m, colRef);
                SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                if (c == null || c.isSetClass1() || (c.isSetHex() == false && 
                    (c.getStringValue() == null || c.getStringValue().isEmpty())))
                {
                    Vector <String> classes = 
                        SyllableConverterUtils.getApplicableClasses(sc, colRef);
                    classes.add(MessageUtil.getString("NoClass"));
                    editor = new ComboBoxCellEditor(composite, 
                        classes.toArray(new String[classes.size()]), SWT.LEAD);
                    if (editor.getControl() != null)
                    {
                        Control control = editor.getControl();
                        if (control instanceof CCombo)
                        {
                            CCombo cc = (CCombo)control;
                            cc.setEditable(true);
                            cc.setText(SyllableConverterUtils.getCText(c));
                        }
                    }
                    if (c != null && c.isSetClass1())
                        editor.setValue(classes.indexOf(c.getClass1()));
                }
                else
                {
                    editor = new TextCellEditor(composite);
                }
            }
            return editor;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
         */
        @Override
        protected Object getValue(Object element)
        {
            if (element instanceof Map)
            {
                Map m = (Map)element;
                C c = SyllableConverterUtils.getCFromMap(m, colRef);
                if (SyllableConverterUtils.getCText(c).isEmpty() || 
                    c.isSetClass1())
                {
                    SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                    if (c == null)
                    {
                        return -1;
                    }
                    Vector <String> classes = 
                        SyllableConverterUtils.getApplicableClasses(sc, c.getR());
                    return (classes.indexOf(c.getClass1()));
                }
                else
                {
                    return SyllableConverterUtils.getCText(c);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
         */
        @Override
        protected void setValue(Object element, Object value)
        {
            if (element instanceof Map)
            {
                Map m = (Map)element;
                SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                
                C c = SyllableConverterUtils.getCFromMap(m, colRef);
                if (c == null)
                {
                    c = m.addNewC();
                    c.setR(colRef);
                }
                String oldValue = SyllableConverterUtils.getCText(c);
                if (SyllableConverterUtils.getCText(c).isEmpty() || 
                    c.isSetClass1())
                {
                    Vector <String> classes = 
                        SyllableConverterUtils.getApplicableClasses(sc, c.getR());
                    if (value instanceof Integer)
                    {
                        int classIndex = ((Integer)value).intValue();
                        if (classIndex < 0)
                        {
                            if (editor.getControl() instanceof CCombo)
                            {
                                CCombo cc = (CCombo)editor.getControl();
                                String newValue = cc.getText();
                                if (newValue != null)
                                {
                                    c.setStringValue(newValue);
                                    if (c.isSetClass1()) c.unsetClass1();
                                    if (c.isSetHex()) c.unsetHex();
                                    parentEditor.setDirty(true);
                                    this.getViewer().refresh(element);
                                }
                            }
                            return;
                        }
                        if (classIndex < classes.size())
                            c.setClass1(classes.get(classIndex));
                        else
                        {
                            if (c.isSetClass1()) c.unsetClass1();
                            if (c.isSetHex()) c.unsetHex();
                        }
                        parentEditor.setDirty(true);
                        this.getViewer().refresh(element);
                    }
                    else
                    {
                        c.setStringValue(value.toString());
                        if (c.isSetClass1()) c.unsetClass1();
                        if (c.isSetHex()) c.unsetHex();
                        parentEditor.setDirty(true);
                        this.getViewer().refresh(element);
                    }
                }
                else
                {
                    if (value.toString().equals(oldValue) == false)
                    {
                        c.setStringValue(value.toString());
                        if (c.isSetHex())
                            c.unsetHex();
                        parentEditor.setDirty(true);
                        this.getViewer().refresh(element);
                    }
                }
            }
        }
        
    }
}
