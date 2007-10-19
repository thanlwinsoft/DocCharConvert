/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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


package org.thanlwinsoft.doccharconvert.opendoc;

import java.util.HashMap;
import java.util.Stack;

import org.thanlwinsoft.xml.ElementProperties;
/**
 *
 * @author keith
 */
public class OpenDocStyleManager
{
  HashMap <OpenDocStyle.StyleFamily, HashMap<String,OpenDocStyle>>familyMap = null;
  Stack <ElementProperties> pendingStyles = null;
  /** Creates a new instance of OpenDocStyleManager */
  public OpenDocStyleManager()
  {
    familyMap = new HashMap <OpenDocStyle.StyleFamily, HashMap<String,OpenDocStyle>>();
    pendingStyles = new Stack<ElementProperties>();
  }
  
  public OpenDocStyle getStyle(String family, String name)
  {
    OpenDocStyle.StyleFamily sFamily = OpenDocStyle.StyleFamily.getType(family);
    if (familyMap.containsKey(sFamily))
    {
      HashMap<String,OpenDocStyle> styleMap = familyMap.get(sFamily);
      return styleMap.get(name);
    }
    return null;
  }
  public OpenDocStyle getStyle(OpenDocStyle.StyleFamily sFamily, String name)
  {
    if (familyMap.containsKey(sFamily))
    {
      HashMap<String,OpenDocStyle> styleMap = familyMap.get(sFamily);
      return styleMap.get(name);
    }
    return null;
  }
  public void addStyle(OpenDocStyle style)
  {
    if (familyMap.containsKey(style.getFamily()))
    {
      HashMap<String,OpenDocStyle> styleMap = familyMap.get(style.getFamily());
      styleMap.put(style.getName(),style);
    }
    else
    {
      HashMap<String,OpenDocStyle> styleMap = new HashMap<String,OpenDocStyle>();
      styleMap.put(style.getName(),style);
      familyMap.put(style.getFamily(), styleMap);
    }
  }
  public void addPendingStyle(ElementProperties pendingStyle)
  {
      pendingStyles.add(pendingStyle);
  }
  public Stack<ElementProperties> getPendingStyles()
  {
      return pendingStyles;
  }
}
