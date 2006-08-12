/*
 * OpenDocStyleManager.java
 *
 * Created on 05 August 2006, 23:56
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.thanlwinsoft.doccharconvert.opendoc;

import java.util.HashMap;
/**
 *
 * @author keith
 */
public class OpenDocStyleManager
{
  HashMap <OpenDocStyle.StyleFamily, HashMap<String,OpenDocStyle>>familyMap = null;
  /** Creates a new instance of OpenDocStyleManager */
  public OpenDocStyleManager()
  {
    familyMap = new HashMap <OpenDocStyle.StyleFamily, HashMap<String,OpenDocStyle>>();
    
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
}
