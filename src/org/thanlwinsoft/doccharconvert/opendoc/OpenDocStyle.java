/*
 * OpenDocStyle.java
 *
 * Created on 05 August 2006, 23:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.thanlwinsoft.doccharconvert.opendoc;

//import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
/**
 * A class to represent an OpenDocument style
 * @author keith
 */
public class OpenDocStyle
{
  protected OpenDocStyle parent = null;
  protected String parentName = null;
  protected String name = null;
  protected StyleFamily styleFamily = null;
  protected String normalFace = "";
  protected String complexFace = "";
  protected String cjkFace = "";
  protected static HashMap <String, StyleFamily> tag2StyleFamily = null;
  
  protected static final Pattern normalizeRegEx = Pattern.compile("[0-9 ]");
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   */
  public OpenDocStyle(String name, String family)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
  }
  public String getName() { return name; }
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   */
  public OpenDocStyle(String name, String family, OpenDocStyle parent)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
    this.parentName = parent.getName();
    this.parent = parent;
  }
  public StyleFamily getFamily() 
  {
    return styleFamily;
  }
  public String getFaceName()
  {
    return this.normalFace;
  }
  public String resolveFaceName()
  {
    OpenDocStyle ods = this;
    while (ods != null && ods.normalFace == null)
    {
      ods = ods.parent;
    }
    if (ods == null) return null;
    return ods.normalFace;
  }
  
  public String getComplexFaceName()
  {
    return this.complexFace;
  }
  public String resolveComplexFaceName()
  {
    OpenDocStyle ods = this;
    while (ods != null && ods.complexFace == null)
    {
      ods = ods.parent;
    }
    if (ods == null) return null;//getFaceName();
    return ods.complexFace;
  }
  
  public String getCjkFaceName()
  {
    return this.cjkFace;
  }
  public String resolveCjkFaceName()
  {
    OpenDocStyle ods = this;
    while (ods != null && ods.cjkFace == null)
    {
      ods = ods.parent;
    }
    if (ods == null) return null;//getFaceName();
    return ods.cjkFace;
  }
  
  public void setFaceName(String faceName)
  {
    this.normalFace = normalizeFace(faceName);
  }
  
  public void  setComplexFaceName(String faceName)
  {
    this.complexFace = normalizeFace(faceName);
  }
  
  public void  setCjkFaceName(String faceName)
  {
    this.cjkFace = normalizeFace(faceName);
  }
  public static String normalizeFace(String faceName)
  {
    String normalized = faceName.toLowerCase();
    normalized = normalizeRegEx.matcher(normalized).replaceAll("");
    return normalized;
  }
  
  /** OpenDocument style families that contain font face information */
  protected enum StyleFamily 
  {
    PARAGRAPH("paragraph"),
    TEXT("text"),
    SECTION("section"),
    TABLE_CELL("table-cell"),
    CHART("chart"),
    GRAPHIC("graphic");
    
    String familyName;
    StyleFamily(String name)
    {
      this.familyName = name;
    }
    public String toString() { return familyName; }
    static StyleFamily getType(String type)
    {
      for (StyleFamily st : StyleFamily.values())
      {
        if (st.familyName.equals(type)) return st;
      }
      return null;
    }
  }
  static 
  {
      tag2StyleFamily = new HashMap <String, StyleFamily>();
      tag2StyleFamily.put("text:p",StyleFamily.PARAGRAPH);
      tag2StyleFamily.put("text:span",StyleFamily.TEXT);
      
  }
  public static StyleFamily getStyleForTag(String tag)
  {
      return tag2StyleFamily.get(tag);
  }
}
