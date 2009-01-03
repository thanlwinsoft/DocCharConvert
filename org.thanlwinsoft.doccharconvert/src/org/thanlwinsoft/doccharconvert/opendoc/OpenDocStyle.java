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

//import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Vector;
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
  protected Vector <OpenDocStyle> altStyles = null;
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
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family as enum
   */
  public OpenDocStyle(StyleFamily family, String name)
  {
    this.name = name;
    this.styleFamily = family;
    this.convertedStyle = this;
  }
  /**
   * 
   * @return style name
   */
  public String getName() { return name; }
  /** Creates a new instance of OpenDocStyle 
   * @param name from style:name attribute
   * @param family from style:family attribute
   * @param parent 
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
 * @param parent 
   */
  public OpenDocStyle(String family, String name, OpenDocStyle parent)
  {
    this.name = name;
    this.styleFamily = StyleFamily.getType(family);
    this.parentName = parent.getName();
    this.parent = parent;
    this.convertedStyle = this;
  }
  /**
   * 
   * @return style family name
   */
  public StyleFamily getFamily() 
  {
    return styleFamily;
  }
  /**
   * 
   * @return latin script face name
   */
  public String getFaceName()
  {
    return this.normalFace;
  }
  /**
   * 
   * @param type
   * @return resolve name from parent styles
   */
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
  /**
   * 
   * @param type
   * @return face name for specified script type
   */
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
  /**
   * find face name looking at parent styles if necessary
   * @return face name
   */
  public String resolveFaceName()
  {
    OpenDocStyle ods = this;
    while (ods != null && ods.normalFace == null)
    {
      ods = ods.parent;
    }
    if (ods == null)
        return null;
    return ods.normalFace;
  }
  /**
   * 
   * @return face name for SE Asian scripts/Arabic
   */
  public String getComplexFaceName()
  {
    return this.complexFace;
  }
  /**
   * 
   * @return parent style
   */
  public OpenDocStyle getParentStyle()
  {
      if (parent == null && parentName != null && manager != null)
      {
          parent = manager.getStyle(this.styleFamily.name(), parentName);
      }
      return parent;
  }
  /**
   * 
   * @return complex face name resolved using parent styles if needed
   */
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
  /**
   * Chinese/Japanese/Korean face name
   * @return face name
   */
  public String getCjkFaceName()
  {
    return this.cjkFace;
  }
  /**
   * 
   * @return Chinese/Japanese/Korean face name resolved using parent styles if needed
   */
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
  /**
   * 
   * @param type
   * @param faceName
   */
  public void setFaceName(ScriptType.Type type, String faceName)
  {
      if (type.equals(ScriptType.Type.CJK))
          this.cjkFace = faceName;
      else if (type.equals(ScriptType.Type.COMPLEX))
          this.complexFace = faceName;
      else
          this.normalFace = faceName;
  }
  /**
   * 
   * @param faceName
   */
  public void setFaceName(String faceName)
  {
    this.normalFace = normalizeFace(faceName);
  }
  /**
   * SE Asian, arabic face
   * @param faceName
   */
  public void  setComplexFaceName(String faceName)
  {
    this.complexFace = normalizeFace(faceName);
  }
  /**
   * Chinese/Japanese/Korean
   * @param faceName
   */
  public void  setCjkFaceName(String faceName)
  {
    this.cjkFace = normalizeFace(faceName);
  }
  /**
   * 
   * @param altStyle
   */
  public void addAltStyle(OpenDocStyle altStyle)
  {
      if (altStyles == null)
          altStyles = new Vector<OpenDocStyle>();
      altStyles.add(altStyle);
  }
  /**
   * 
   * @param script
   * @param face
   * @return styles that may be used if a conversion is performed
   */
  public OpenDocStyle getAltStyle(ScriptType.Type script, String face)
  {
      if (altStyles == null) return null;
      for (OpenDocStyle s : altStyles)
      {
          String styleFace = s.getFaceName(script);
          if (styleFace != null && styleFace.equals(face))
              return s;
      }
      return null;
  }
  /**
   * 
   * @param faceName
   * @return face name without bad characters
   */
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
  /**
   * 
   * @param tag
   * @return family
   */
  public static StyleFamily getStyleForTag(String tag)
  {
      return tag2StyleFamily.get(tag);
  }
  /**
   * 
   * @param m
   */
  public void setManager(OpenDocStyleManager m)
  {
      this.manager = m;
  }
  /**
   * 
   * @return style to use for converted text
   */
  public OpenDocStyle getConvertedStyle()
  {
      return convertedStyle;
  }
  /**
   * 
   * @param converted
   */
  public void setConvertedStyle(OpenDocStyle converted)
  {
      this.convertedStyle = converted;
  }
  @Override
  public String toString()
  {
      return new String(this.name + ">" + this.convertedStyle);
  }
}
