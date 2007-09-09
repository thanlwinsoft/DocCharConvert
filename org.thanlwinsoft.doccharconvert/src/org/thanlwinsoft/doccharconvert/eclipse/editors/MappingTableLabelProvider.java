/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.Map;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
/**
 * @author keith
 *
 */
public class MappingTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider 
{
    private MappingTable mappingTable;
    public MappingTableLabelProvider(MappingTable mappingTable)
    {
        this.mappingTable = mappingTable;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex)
    {
        String ref = mappingTable.getColumns().getComponentArray(columnIndex).getR();
        if (element instanceof Map)
        {
            Map m = (Map)element;
            return SyllableConverterUtils.getCText(SyllableConverterUtils.getCFromMap(m, ref));
        }
        return null;
    }
    
}
