/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.util.Pair;

/**
 * @author keith
 *
 */
public class ClassTableEditorPart extends EditorPart
{
    private Table table; 
    private TableViewer viewer;
    private final SyllableConverterEditor parentEditor;
    private org.thanlwinsoft.schemas.syllableParser.Class classTable;
    //private Clipboard clipboard;
    private MenuManager menuManager;
    public ClassTableEditorPart(SyllableConverterEditor parentEditor, 
        org.thanlwinsoft.schemas.syllableParser.Class clazz)
    {
        this.classTable = clazz;
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
        //clipboard = new Clipboard(site.getShell().getDisplay());
        final IEditorPart part = this;
        menuManager = new MenuManager(parentEditor.getPartName() + ":" + classTable.getId());
        Action insertAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0)
                    mapIndex = classTable.getComponentArray(0).sizeOfCArray();
                int insertRowCount = Math.max(1, table.getSelectionCount());                
                for (int i = 0; i < insertRowCount; i++)
                {
                    classTable.getComponentArray(0).insertNewC(mapIndex);
                    classTable.getComponentArray(1).insertNewC(mapIndex);
                }
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        insertAction.setId("Insert");
        insertAction.setText(MessageUtil.getString("Insert"));
        insertAction.setToolTipText(MessageUtil.getString("InsertToolTip"));
        Action deleteAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0)
                    return;
                classTable.getComponentArray(0).removeC(mapIndex);
                classTable.getComponentArray(1).removeC(mapIndex);
                
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        deleteAction.setId("Delete");
        deleteAction.setText(MessageUtil.getString("Delete"));
        deleteAction.setToolTipText(MessageUtil.getString("DeleteToolTip"));
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
                for (int i = 0; i < sc.getClasses().sizeOfClass1Array(); i++)
                {
                    if (sc.getClasses().getClass1Array(i) == classTable)
                    {
                        sc.getClasses().removeClass1(i);
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
    }

    /**
     * @return
     */
    protected int getSelectedMapIndex()
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return -1;
        IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
        if (s.getFirstElement() instanceof Pair<?,?>)
        {
            Pair<?,?> selectedPair = (Pair<?,?>)s.getFirstElement();
            for (int i = 0; i < classTable.getComponentArray(0).sizeOfCArray(); i++)
            {
                if (classTable.getComponentArray(0).getCArray(i) == selectedPair.first)
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
        viewer.setContentProvider(new ClassTableContentProvider());
        for (int colIndex = 0; colIndex < classTable.sizeOfComponentArray(); colIndex++)
        {
            ComponentRef cr = classTable.getComponentArray(colIndex);
            final String colRef = cr.getR();
            final int col = colIndex;
            TableColumn tc = new TableColumn(table, SWT.LEAD);
            tc.setText(cr.getR());
            tc.setWidth(100);
            TableViewerColumn tvc = new TableViewerColumn(viewer, tc);
            SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
            tc.setToolTipText(SyllableConverterUtils.getComponentName(sc, colRef));
            tvc.setEditingSupport(new CellEditingSupport(viewer, colRef, colIndex));
            tvc.setLabelProvider(new CellLabelProvider(){
                
                @Override
                public void update(ViewerCell cell)
                {
                    Object o = cell.getViewerRow().getElement();
                    if (o instanceof Pair<?,?>)
                    {
                        Object cValue = ((Pair<?,?>)o).get(col);
                        if (cValue instanceof C)
                        {
                            cell.setText(SyllableConverterUtils.getCText((C)cValue));
                        }
                    }
                }});
        }
        viewer.setInput(classTable);
        table.setHeaderVisible(true);
        viewer.refresh();
        menuManager.add(new Separator());
        menuManager.add(new GroupMarker (IWorkbenchActionConstants.MB_ADDITIONS));
        this.getEditorSite().registerContextMenu(menuManager, viewer);
        this.getEditorSite().setSelectionProvider(viewer);
        menuManager.setVisible(true);
        table.setMenu(menuManager.createContextMenu(table));
        table.setToolTipText(MessageUtil.getString("ClassTableToolTip"));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        table.setFocus();
    }
    
    public class CellEditingSupport extends EditingSupport
    {
        final String colRef;
        final int colIndex;
        CellEditor editor;
        /**
         * @param viewer
         */
        public CellEditingSupport(ColumnViewer viewer, String colRef, int colIndex)
        {
            super(viewer);
            this.colRef = colRef;
            this.colIndex = colIndex;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
         */
        @Override
        protected boolean canEdit(Object element)
        {
            if (element instanceof Pair<?,?>)
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
            if (editor == null) editor = new TextCellEditor(table);
            return editor;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
         */
        @Override
        protected Object getValue(Object element)
        {
            if (element instanceof Pair<?,?>)
            {
                C c = (C)((Pair<?,?>)element).get(colIndex);
                return SyllableConverterUtils.getCText(c);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
         */
        @Override
        protected void setValue(Object element, Object value)
        {
            Object o = ((Pair<?,?>)element).get(colIndex);
            if (o instanceof C)
            {
                C c = (C)o;
                String oldValue = SyllableConverterUtils.getCText(c);
                if (value != null && !oldValue.equals(value))
                {
                    c.setStringValue(value.toString());
                    viewer.refresh(element);
                }
                else c.setStringValue("");
            }
        }

    }
    
}
