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
  protected OpenDocStyleManager manager = null;
  protected OpenDocStyle parent = null;
  protected String parentName = null;
  protected String name = null;
  protected StyleFamily styleFamily = null;
  protected String normalFace = null;
  protected String complexFace = null;
  protected String cjkFace = null;
  protected OpenDocStyle convertedStyle;
  protected static HashMap <String, StyleFamily> tag2StyleFamily = null;
  
  protected static final Pattern normalizeRegEx = Pattern.compile("[0-9 ]");
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   */
  public OpenDocStyle(String family, String name)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
    this.convertedStyle = this;
  }
  public String getName() { return name; }
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   */
  public OpenDocStyle(String family, String name, String parent)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
    this.parentName = parent;
    this.parent = null;
    this.convertedStyle = this;
  }
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   */
  public OpenDocStyle(String family, String name, OpenDocStyle parent)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
    this.parentName = parent.getName();
    this.parent = parent;
    this.convertedStyle = this;
  }
  public StyleFamily getFamily() 
  {
    return styleFamily;
  }
  public String getFaceName()
  {
    return this.normalFace;
  }
  public String resolveFaceName(ScriptType.Type type)
  {
      String name;
      if (type.equals(ScriptType.Type.CJK))
      {
          name = resolveCjkFaceName();
      }
      else if (type.equals(ScriptType.Type.COMPLEX))
      {
          name = resolveComplexFaceName();
      }
      else
      {
          name = resolveFaceName();
      }
      return name;
  }
  public String getFaceName(ScriptType.Type type)
  {
      String name;
      if (type.equals(ScriptType.Type.CJK))
      {
          name = getCjkFaceName();
      }
      else if (type.equals(ScriptType.Type.COMPLEX))
      {
          name = getComplexFaceName();
      }
      else
      {
          name = getFaceName();
      }
      return name;
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
  public OpenDocStyle getParentStyle()
  {
      if (parent == null && parentName != null && manager != null)
      {
          parent = manager.getStyle(this.styleFamily.name(), parentName);
      }
      return parent;
  }
  public String resolveComplexFaceName()
  {
    OpenDocStyle ods = this;
    if (parent == null && parentName != null && manager != null)
    {
        parent = manager.getStyle(this.styleFamily.name(), parentName);
    }
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
  public void setFaceName(ScriptType.Type type, String faceName)
  {
      if (type.equals(ScriptType.Type.CJK))
          this.cjkFace = faceName;
      else if (type.equals(ScriptType.Type.COMPLEX))
          this.complexFace = faceName;
      else
          this.normalFace = faceName;
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
    TABLE("table"),
    TABLE_ROW("table-row"),
    TABLE_CELL("table-cell"),
    TABLE_COLUMN("table-column"),
    CHART("chart"),
    PRESENTATION("presentation"),
    DRAWING_PAGE("drawing-page"),
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
      tag2StyleFamily.put("text:h",StyleFamily.PARAGRAPH);
      tag2StyleFamily.put("text:span",StyleFamily.TEXT);
      tag2StyleFamily.put("table:table",StyleFamily.TABLE);
      tag2StyleFamily.put("table:table-cell",StyleFamily.TABLE_CELL);
      tag2StyleFamily.put("table:table-row",StyleFamily.TABLE_CELL);
      tag2StyleFamily.put("table:table-column",StyleFamily.TABLE_CELL);
      //tag2StyleFamily.put("draw:frame",StyleFamily.PRESENTATION);
      tag2StyleFamily.put("draw:frame",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:custom-shape",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:ellipse",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:line",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:text-box",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:rect",StyleFamily.GRAPHIC);
      tag2StyleFamily.put("draw:path",StyleFamily.GRAPHIC);
      
  }
  public static StyleFamily getStyleForTag(String tag)
  {
      return tag2StyleFamily.get(tag);
  }
  public void setManager(OpenDocStyleManager m)
  {
      this.manager = m;
  }
  public OpenDocStyle getConvertedStyle()
  {
      return convertedStyle;
  }
  public void setConvertedStyle(OpenDocStyle converted)
  {
      this.convertedStyle = converted;
  }
  public String toString()
  {
      return new String(this.name + ">" + this.convertedStyle);
  }
}
