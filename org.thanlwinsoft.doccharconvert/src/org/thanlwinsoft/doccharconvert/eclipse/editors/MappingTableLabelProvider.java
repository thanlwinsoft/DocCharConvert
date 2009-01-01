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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.syllable.MappingStatus;
import org.thanlwinsoft.schemas.syllableParser.Map;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;

/**
 * @author keith
 * 
 */
public class MappingTableLabelProvider extends CellLabelProvider implements
        ITableLabelProvider, ITableFontProvider, ITableColorProvider
{
    private MappingTable mappingTable;
    private SyllableConverterEditor mParentEditor;
    public static final int COL_OFFSET = 1;
    private static final int STATUS_COLOR = 255;
    private static final int NO_STATUS_COLOR = 172;
    private static Color mForwardsColor = null;
    private static Color mBothColor = null;
    private static Color mBackwardsColor = null;

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#dispose(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    public void dispose(ColumnViewer viewer, ViewerColumn column)
    {
        if (mForwardsColor != null && !mForwardsColor.isDisposed())
            mForwardsColor.dispose();
        if (mBackwardsColor != null && !mBackwardsColor.isDisposed())
            mBackwardsColor.dispose();
        if (mBothColor != null && !mBothColor.isDisposed())
            mBothColor.dispose();
        
        super.dispose(viewer, column);
    }

    public MappingTableLabelProvider(MappingTable mappingTable,
            SyllableConverterEditor parent)
    {
        this.mappingTable = mappingTable;
        mParentEditor = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
     * .Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected int findMapNumber(Object element)
    {
        if (element instanceof Map)
        {
            Map m = (Map) element;
            for (int i = 0; i < mappingTable.getMaps().sizeOfMArray(); i++)
            {
                if (mappingTable.getMaps().getMArray(i) == m)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText(Object element)
    {
        int row = findMapNumber(element);
        if (mParentEditor.getConverter() != null)
        {
            int status = mParentEditor.getConverter().getMapStatus(
                    mappingTable.getId(), row);
            if (status > 0)
            {
                String msgPrefix = "ambiguous";
                if (mappingTable.isSetFirstEntryWins() && mappingTable.getFirstEntryWins())
                {
                    msgPrefix = "unused";
                }
                if (MappingStatus.AMBIGUOUS_FORWARDS.isSet(status))
                {
                    if (MappingStatus.AMBIGUOUS_BACKWARDS.isSet(status))
                    {
                        return MessageUtil.getString(msgPrefix + "Both");
                    }
                    else
                    {
                        return MessageUtil.getString(msgPrefix + "Forwards");
                    }
                }
                else if (MappingStatus.AMBIGUOUS_BACKWARDS.isSet(status))
                {
                    return MessageUtil.getString(msgPrefix + "Backwards");
                }
            }
        }
        return MessageUtil.getString("MappingTableToolTip");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
     * .Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        if (element instanceof Map)
        {
            Map m = (Map) element;
            if (columnIndex < COL_OFFSET)
            {
                int line = findMapNumber(element) + 1;
                if (line > 0)
                    return Integer.toString(line);
                return "";
            }
            String ref = mappingTable.getColumns().getComponentArray(
                    columnIndex - COL_OFFSET).getR();
            return SyllableConverterUtils.getCText(SyllableConverterUtils
                    .getCFromMap(m, ref));
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    @Override
    public Font getFont(Object element, int columnIndex)
    {
        if (columnIndex < COL_OFFSET)
        {
            return JFaceResources.getFont(JFaceResources.TEXT_FONT);
        }
        String ref = mappingTable.getColumns().getComponentArray(
                columnIndex - COL_OFFSET).getR();
        SyllableConverter sc = mParentEditor.getDocument()
                .getSyllableConverter();
        int side = SyllableConverterUtils.getSide(sc, ref);
        return mParentEditor.getFont(side);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.
     * jface.viewers.ILabelProviderListener)
     */
    @Override
    public void addListener(ILabelProviderListener listener)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang
     * .Object, java.lang.String)
     */
    @Override
    public boolean isLabelProperty(Object element, String property)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
     * .jface.viewers.ILabelProviderListener)
     */
    @Override
    public void removeListener(ILabelProviderListener listener)
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.
     * viewers.ViewerCell)
     */
    @Override
    public void update(ViewerCell cell)
    {
        cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
        if (cell.getColumnIndex() >= COL_OFFSET)
            cell.setFont(getFont(cell.getElement(), cell.getColumnIndex()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang
     * .Object, int)
     */
    @Override
    public Color getBackground(Object element, int columnIndex)
    {
        // if (columnIndex == 0)
        {
            int row = findMapNumber(element);
            if (mParentEditor.getConverter() != null)
            {
                int status = mParentEditor.getConverter().getMapStatus(
                        mappingTable.getId(), row);
                if (status > 0)
                {
                    int red = MappingStatus.AMBIGUOUS_FORWARDS.isSet(status) ? STATUS_COLOR
                            : NO_STATUS_COLOR;
                    int blue = MappingStatus.AMBIGUOUS_BACKWARDS.isSet(status) ? STATUS_COLOR
                            : NO_STATUS_COLOR;

                    Color color = null;
                    if (status == (MappingStatus.AMBIGUOUS_FORWARDS.bit() & 
                        MappingStatus.AMBIGUOUS_BACKWARDS.bit()))
                    {
                        if (mBothColor == null)
                        {
                            mBothColor = new Color(mParentEditor.getSite().getShell()
                                    .getDisplay(), red, NO_STATUS_COLOR, blue);
                        }
                        color = mBothColor;
                    }
                    else if (status == MappingStatus.AMBIGUOUS_FORWARDS.bit())
                    {
                        if (mForwardsColor == null)
                        {
                            mForwardsColor = new Color(mParentEditor.getSite().getShell()
                                    .getDisplay(), red, NO_STATUS_COLOR, blue);
                        }
                        color = mForwardsColor;
                    }
                    else if (status == MappingStatus.AMBIGUOUS_BACKWARDS.bit())
                    {
                        if (mBackwardsColor == null)
                        {
                            mBackwardsColor = new Color(mParentEditor.getSite().getShell()
                                    .getDisplay(), red, NO_STATUS_COLOR, blue);
                        }
                        color = mBackwardsColor;
                    }
                    return color;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang
     * .Object, int)
     */
    @Override
    public Color getForeground(Object element, int columnIndex)
    {
        return null;
    }

}
