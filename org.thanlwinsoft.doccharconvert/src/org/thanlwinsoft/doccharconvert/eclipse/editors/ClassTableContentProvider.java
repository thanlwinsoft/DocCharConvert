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

import java.util.ArrayList;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.util.Pair;
/**
 * @author keith
 *
 */
public class ClassTableContentProvider implements IContentProvider,
    IStructuredContentProvider
{

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        if (inputElement instanceof org.thanlwinsoft.schemas.syllableParser.Class)
        {
            org.thanlwinsoft.schemas.syllableParser.Class clazz = 
                (org.thanlwinsoft.schemas.syllableParser.Class)inputElement;
            ComponentRef a = clazz.getComponentArray(0);
            ComponentRef b = clazz.getComponentArray(1);
            ArrayList<Pair<C,C> > elements = new ArrayList<Pair<C,C>> ();
            for (int i = 0; i < a.sizeOfCArray() && i < b.sizeOfCArray(); i++)
            {
                elements.add(new Pair<C,C>(a.getCArray(i), b.getCArray(i)));
            }
            return elements.toArray(new Pair<?,?>[elements.size()]);
        }
        return null;
    }

}
