/*
Copyright (C) 2009 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.odf;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfDocument.OdfMediaType;
import org.odftoolkit.odfdom.doc.office.OdfOfficeDrawing;
import org.odftoolkit.odfdom.doc.office.OdfOfficeFontFaceDecls;
import org.odftoolkit.odfdom.doc.office.OdfOfficePresentation;
import org.odftoolkit.odfdom.doc.office.OdfOfficeSpreadsheet;
import org.odftoolkit.odfdom.doc.office.OdfOfficeText;
import org.odftoolkit.odfdom.doc.office.OdfOfficeBody;
import org.odftoolkit.odfdom.doc.style.OdfDefaultStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyleFontFace;
import org.odftoolkit.odfdom.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.doc.text.OdfTextSpace;
import org.odftoolkit.odfdom.doc.text.OdfTextSpan;
import org.odftoolkit.odfdom.doc.text.OdfTextTab;
import org.odftoolkit.odfdom.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.OdfName;
import org.odftoolkit.odfdom.OdfNamespace;
import org.odftoolkit.odfdom.OdfElement;
import org.odftoolkit.odfdom.dom.OdfNamespaceNames;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.element.OdfStyleBase;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptSegment;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptType;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptType.Type;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Document Converter Interface using ODF DOM
 * @author keith
 */
public class OdfDocInterface implements DocInterface {

	private HashMap<String, HashMap<CharConverter, String> > mConvertedStyles = null;
	private HashMap<String, String> mFaceMap = null;
	private int mTextStyleCount = 0;
	private ConversionMode mMode = null;
	private boolean mAbort = false;
	private static Pattern sWhitespace = Pattern.compile("^\\s+$"); 
	@Override
	public void abort()
	{
		mAbort = true;
	}

	@Override
	public void destroy()
	{
	
	}

	@Override
	public ConversionMode getMode()
	{
		return mMode;
	}

	@Override
	public String getStatusDesc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialise() throws InterfaceException
	{
		mAbort = false;
		mTextStyleCount = 0;
		mConvertedStyles = new HashMap<String, HashMap<CharConverter, String> >();
		mFaceMap = new HashMap<String,String>();
	}

	@Override
	public void parse(File input, File output,
			Map<TextStyle, CharConverter> converters, ProgressNotifier notifier)
			throws FatalException, InterfaceException, WarningException
	{
		System.out.println(input);
		try
		{
			OdfDocument odfInput = OdfDocument.loadDocument(input);
			parseDocument(odfInput, converters);
			
			List<OdfDocument> embedded = odfInput.getEmbeddedDocuments();
			for (OdfDocument doc : embedded)
			{
				if (mAbort) break;
				parseDocument(doc, converters);
			}
			
			if (!mAbort)
				odfInput.save(output);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FatalException(e.getLocalizedMessage());
		}
	}
	
	private void parseDocument(OdfDocument doc, Map<TextStyle, CharConverter> converters) throws Exception
	{
		initialise();
		OdfMediaType type = OdfMediaType.getOdfMediaType(doc.getMediaType());
		if (type.equals(OdfMediaType.TEXT) || type.equals(OdfMediaType.SPREADSHEET))
		{
			parseFontFaces(doc);
		}
		
		OdfOfficeBody body = doc.getOfficeBody();
		
		Deque <OdfStyleBase> styleStack = new ArrayDeque<OdfStyleBase>();
		OdfDefaultStyle defaultStyle =
			doc.getDocumentStyles().getDefaultStyle(OdfStyleFamily.Paragraph);
		if (defaultStyle != null) styleStack.push(defaultStyle);
		
		Deque <ConvertibleTextFragment> textFrags = new ArrayDeque <ConvertibleTextFragment>();
		ConvertibleTextFragment prevText = null;

		parseNodes(doc, body, converters, styleStack, textFrags, prevText);
		if (!mAbort)
			convertNodes(doc, body, textFrags);
	}
	
	private void parseFontFaces(OdfDocument doc) throws Exception
	{
		// check that there is a font-face element for the font-name
		Element docElement = doc.getContentDom().getDocumentElement();
		if (docElement instanceof OdfElement)
		{
			OdfOfficeFontFaceDecls faceDecls = OdfElement.findFirstChildNode(OdfOfficeFontFaceDecls.class, (OdfElement)docElement);
			OdfStyleFontFace face = OdfElement.findFirstChildNode(OdfStyleFontFace.class, faceDecls);
			while (face != null)
			{
				mFaceMap.put(face.getStyleNameAttribute(), face.getSvgFontFamilyAttribute());
				face = OdfElement.findNextChildNode(OdfStyleFontFace.class, face);
			}
		}
	}

	private void convertNodes(OdfDocument doc, OdfElement para,
			Deque<ConvertibleTextFragment> textFrags) throws Exception
	{
		OdfFileDom dom = (OdfFileDom)para.getOwnerDocument();
		while (textFrags.size() > 0)
		{
			ConvertibleTextFragment frag = textFrags.removeFirst();
						
			String text = frag.toString();
			try
			{
				String converted = "";
				if (!frag.isConverted())
				{
					converted = frag.getConverter().convert(text);
					// check whether converting adjacent nodes together produces
					// a different conversion result, if so there is probably
					// a style break mid syllable
					String cumulative = "";
					ConvertibleTextFragment prevFrag = frag;
					for (ConvertibleTextFragment next : textFrags)
					{
						if (next.previousFragment() == prevFrag &&
							prevFrag.getConverter() == next.getConverter())
						{
							String combined = frag.getConverter()
								.convert(text + cumulative + next.toString());
							String nextConverted = frag.getConverter()
								.convert(cumulative + next.toString());
							// assume that we are going to unicode most of the time
							// check that we aren't breaking the style at a 
							// character with unicode property 
							int type = Character.OTHER_LETTER;
							if (nextConverted.length() > 0)
							{
								type = Character.getType(nextConverted.charAt(0));
								// HACK for Unicode 5.1 while java is on 5.0
								switch (nextConverted.charAt(0))
								{
								case 0x102B:
								case 0x103B:
								case 0x103C:
									type = Character.COMBINING_SPACING_MARK;
									break;
								case 0x103A:
								case 0x103D:
								case 0x103E:
									type = Character.NON_SPACING_MARK;
									break;
								}
							}
							
							if (!midSyllable(type) &&
								combined.equals(converted + nextConverted) == true)
							{
								prevFrag = next;
								cumulative += next.toString();
							}
							else
							{
								cumulative += next.toString();
								converted = combined;
								next.setConverted(true);
								prevFrag = next;
								for (ConvertibleTextFragment interFrag: textFrags)
								{
									interFrag.setConverted(true);
									if (interFrag == next)
										break;
								}
							}
						}
						else break;
					}
				}
				Node parent = frag.getNode().getParentNode();
				if (parent instanceof OdfTextSpan)
				{
					// easy case - whole of text span
					if (parent.getChildNodes().getLength() == 1 &&
						frag.getStart() == 0 &&
						frag.getLength() == frag.getNode().getLength())
					{
						OdfTextSpan span = (OdfTextSpan)parent;
						Text convertedNode = parent.getOwnerDocument()
							.createTextNode(converted);
						span.replaceChild(convertedNode, frag.getNode());
						span.setStyleName(getConvertedStyle(doc, 
							frag.getConverter(), span.getStyleName()));
					}
					else
					{
						// span <span>xyz</span>
						// to   <span>x</span><span>Y</span><span>z</span>
						OdfTextSpan span = (OdfTextSpan)parent;
						OdfTextSpan convertedSpan = new OdfTextSpan(dom);
						convertedSpan.setStyleName(getConvertedStyle(doc, 
								frag.getConverter(), span.getStyleName()));
						
						OdfTextSpan postSpan = null;
						int textNodeIndex;
						for (textNodeIndex = 0; textNodeIndex <
							 span.getChildNodes().getLength();
						     textNodeIndex++)
						{
							if (span.getChildNodes().item(textNodeIndex) == 
								frag.getNode())
							{
								break;
							}
						}
						assert(textNodeIndex < span.getLength());
						if (textNodeIndex + 1 < span.getLength() || 
							frag.getStart() + frag.getLength() <
							frag.getNode().getLength())
						{
							postSpan = new OdfTextSpan(dom);
							postSpan.setStyleName(span.getStyleName());
						}
						
						Text convertedNode = span.getOwnerDocument()
							.createTextNode(converted);
						convertedSpan.appendChild(convertedNode);

						if (postSpan != null)
						{
							if (frag.getStart() + frag.getLength() < 
								frag.getNode().getLength())
							{
								String postfix = frag.getNode().getData()
									.substring(frag.getStart() + frag.getLength());
								Text postfixNode = span.getOwnerDocument()
									.createTextNode(postfix);
								postSpan.appendChild(postfixNode);
								// fix up any other fragments sharing this node
								for (ConvertibleTextFragment postFrag : textFrags)
								{
									if (postFrag.getNode() != frag.getNode()) break;
									assert(postFrag.getStart() >=
										   frag.getStart() + frag.getLength());
									postFrag.setNode(postfixNode);
									postFrag.setStart(postFrag.getStart() 
										- frag.getStart() - frag.getLength());
								}
							}
							Node paraLevelParent = getTopLevelParent(span);
							
							// fix up subsequent nodes with same parent span
							for (int i = textNodeIndex + 1; i < span.getLength(); i++)
							{
								Node n = span.getChildNodes().item(i);
								Node clone = n.cloneNode(true);
								postSpan.appendChild(clone);
								// fix up subsequent fragments
								for (ConvertibleTextFragment postFrag : textFrags)
								{
									if (postFrag.getNode() == n)
									{
										postFrag.setNode((Text)clone);
									}
									else if (postFrag.getNode().getParentNode() == n)
									{
										int fragIndex;
										for (fragIndex = 0; fragIndex < 
											 n.getChildNodes().getLength();
											 fragIndex++)
										{
											if (n.getChildNodes().item(fragIndex)
												== postFrag.getNode())
												break;
										}
										assert(fragIndex < clone.getChildNodes()
											   .getLength());
										postFrag.setNode((Text)clone.getChildNodes()
												         .item(fragIndex));
									}
									else
									{
										// the next line
										// may be null when it is run on a 
										// parent less clone
										Node postParaLevel = getTopLevelParent(postFrag.getNode());
										Node ancestor = postFrag.getNode().getParentNode();
										while (ancestor != null && ancestor != postParaLevel)
										{
											if (ancestor == n)
											{
												System.out.println("deep ancestor needs attention");
											}
											ancestor = ancestor.getParentNode();
										}
										// stop looping over the whole stack
										// unnecessarily 
										if (postParaLevel != null &&
											postParaLevel != paraLevelParent)
											break;
									}
									
								}
							}
						}
						// remove duplicate nodes after the original node
						for (int i = span.getLength() - 1; i > textNodeIndex; i--)
						{
							span.removeChild(span.getChildNodes().item(i));
						}
						// now insert the nodes
						if (span.getNextSibling() == null)
						{
							span.getParentNode().appendChild(convertedSpan);
						}
						else
						{
							span.getParentNode().insertBefore(convertedSpan, span.getNextSibling());
						}
						if (frag.getStart() > 0)
						{
							String prefix = frag.getNode().getData().substring(0, frag.getStart());
							Text prefixNode = span.getOwnerDocument().createTextNode(prefix);
							span.appendChild(prefixNode);
						}
						if (postSpan != null)
						{
							if (convertedSpan.getNextSibling() == null)
							{
								convertedSpan.getParentNode().appendChild(postSpan);
							}
							else
							{
								convertedSpan.getParentNode().insertBefore(postSpan, convertedSpan.getNextSibling());
							}
						}
						// finally remove the original node
						span.removeChild(frag.getNode());
						
					}
				}
				else
				{
					// paragraph case <p>xyz</p>
					// to <p>x<span>Y</span>z</p>
					OdfTextSpan convertedSpan = new OdfTextSpan(dom);
					String styleName = "";
					if (frag.getNode() instanceof OdfStylableElement)
					{
						styleName = ((OdfStylableElement)frag.getNode()).getStyleName();
					}
					convertedSpan.setStyleName(getConvertedStyle(doc, frag.getConverter(), styleName));
					Text convertedNode = frag.getNode().getOwnerDocument().createTextNode(converted);
					convertedSpan.appendChild(convertedNode);
					
					if (frag.getNode().getNextSibling() == null)
					{
						if (frag.getNode().getParentNode() != null)
							frag.getNode().getParentNode().appendChild(convertedSpan);
					}
					else
					{
						if (frag.getNode().getParentNode() != null)
							frag.getNode().getParentNode().insertBefore(convertedSpan, frag.getNode().getNextSibling());
					}
					if (frag.getStart() > 0)
					{
						String prefix = frag.getNode().getData().substring(0, frag.getStart());
						Text prefixNode = frag.getNode().getOwnerDocument().createTextNode(prefix);
						if (frag.getNode().getParentNode() != null)
							frag.getNode().getParentNode().insertBefore(prefixNode, convertedSpan);
					}
					if (frag.getStart() + frag.getLength() < frag.getNode().getLength())
					{
						String postfix = frag.getNode().getData().substring(frag.getStart() + frag.getLength());
						Text postfixNode = frag.getNode().getOwnerDocument().createTextNode(postfix);
						if (convertedSpan.getNextSibling() == null)
						{
							if (frag.getNode().getParentNode() != null)
								frag.getNode().getParentNode().appendChild(postfixNode);
						}
						else
						{
							if (frag.getNode().getParentNode() != null)
								frag.getNode().getParentNode().insertBefore(postfixNode, convertedSpan.getNextSibling());
						}
						// are there any more fragments using the same text node? If so fix them.
						for (ConvertibleTextFragment f : textFrags)
						{
							if (f.getNode() != frag.getNode()) break;
							assert(f.getStart() >= frag.getStart() + frag.getLength());
							f.setStart(f.getStart() - frag.getStart() - frag.getLength());
							f.setNode(postfixNode);
						}	
					}
					// finally remove the original node
					if (frag.getNode().getParentNode() != null)
						frag.getNode().getParentNode().removeChild(frag.getNode());
					else
						System.out.println("Orphaned node " + frag);
				}
			}
			catch(RecoverableException e)
			{
				DocCharConvertEclipsePlugin.log(2/*warning*/, "Convert node error", e);
			}
		}
	}
	
	private boolean midSyllable(int type)
	{
		switch (type)
		{
		case Character.NON_SPACING_MARK:
		case Character.COMBINING_SPACING_MARK:
			return true;
		}
		return false;
	}

	private OdfElement getTopLevelParent(Node n)
	{
		Node paraLevelParent = n;
		while (paraLevelParent != null &&
			   !(paraLevelParent.getParentNode() instanceof OdfOfficeText) &&
			   !(paraLevelParent.getParentNode() instanceof OdfOfficePresentation) &&
			   !(paraLevelParent.getParentNode() instanceof OdfOfficeDrawing) &&
			   !(paraLevelParent.getParentNode() instanceof OdfOfficeSpreadsheet))
		{
			paraLevelParent = paraLevelParent.getParentNode();
		}
		if (paraLevelParent == null)
		{
			return null;
		}
		return (OdfElement)paraLevelParent;
	}

	private String getConvertedStyle(OdfDocument doc, CharConverter converter, String styleName) throws Exception
	{
		// no need to create a new style if font name doesn't change
		if (converter.getNewStyle().getScriptType().equals(
				converter.getOldStyle().getScriptType()) &&
			converter.getNewStyle().getFontName().equals(
				converter.getOldStyle().getFontName()))
			return styleName;
		
		if (mConvertedStyles.containsKey(styleName))
		{
			if (mConvertedStyles.get(styleName).containsKey(converter))
			{
				String convertedStyle = mConvertedStyles.get(styleName).get(converter);
				//System.out.println("Cached Orig:" + styleName + ">" + convertedStyle);
				return convertedStyle;
			}

		}
		else
		{
			mConvertedStyles.put(styleName, new HashMap<CharConverter,String>());
		}
		// need to create a new style
		//OdfStyles styles = doc.getOrCreateDocumentStyles();
		OdfOfficeAutomaticStyles autoStyles = doc.getContentDom().getAutomaticStyles();
		String name = "T" + (++mTextStyleCount);
		while (autoStyles.getStyle(name, OdfStyleFamily.Text) != null)
		{
			name = "T" + (++mTextStyleCount);
		}
		OdfStyleBase origStyle = autoStyles.getStyle(styleName, OdfStyleFamily.Text);
		if (origStyle == null)
			origStyle = autoStyles.getStyle(styleName, OdfStyleFamily.Paragraph);
		if (origStyle == null)
			origStyle = doc.getOrCreateDocumentStyles().getStyle(styleName, OdfStyleFamily.Text);
		if (origStyle == null)
			origStyle = doc.getOrCreateDocumentStyles().getStyle(styleName, OdfStyleFamily.Paragraph);
		if (origStyle == null)
		{
			//System.out.println("Warning: orig style not found - " + styleName);
		}
		String fontSize = "";
		String fontSizeRel = "";
		String fontPitch = "";
		String fontStyleName = "";
		String fontFamilyGeneric = "";
		String fontStyle = "";
		String fontWeight = "";
		while (origStyle != null)
		{
			StyleTextPropertiesElement oldProps = (StyleTextPropertiesElement)origStyle.getOrCreatePropertiesElement(OdfStylePropertiesSet.TextProperties);
			final OdfNamespace fo = OdfNamespace.newNamespace(OdfNamespaceNames.FO);
			final OdfNamespace style = OdfNamespace.newNamespace(OdfNamespaceNames.STYLE);
			fontSize = parseProperty(converter, oldProps, fo,
					"font-size", fontSize);
			fontSizeRel = parseProperty(converter, oldProps, fo,
					"font-size-rel", fontSizeRel);
			fontPitch = parseProperty(converter, oldProps, style, 
					"font-pitch", fontPitch);
			fontStyleName = parseProperty(converter, oldProps, style, 
					"font-style-name", fontStyleName);
			fontFamilyGeneric = parseProperty(converter, oldProps, style, 
					"font-family-generic", fontFamilyGeneric);
			fontStyle = parseProperty(converter, oldProps, fo, 
					"font-style", fontStyle);
			fontWeight = parseProperty(converter, oldProps, fo, 
					"font-weight", fontWeight);
			
			origStyle = origStyle.getParentStyle();
		}
		
		OdfStyle style = autoStyles.newStyle(OdfStyleFamily.Text);
		if (styleName.length() > 0)
		{
			style.setStyleParentStyleNameAttribute(styleName);
		}
		else
		{
			
		}
		style.setStyleNameAttribute(name);
		StyleTextPropertiesElement props = (StyleTextPropertiesElement) style.getOrCreatePropertiesElement(OdfStylePropertiesSet.TextProperties);
		// TODO support font-family
		Type type = converter.getNewStyle().getScriptType();
		String newFontName = converter.getNewStyle().getFontName();
		int faceCount = 0;

		OdfNamespace styleNS = OdfNamespace.newNamespace(OdfNamespaceNames.STYLE);
		OdfNamespace svgNS = OdfNamespace.newNamespace(OdfNamespaceNames.SVG);
		OdfNamespace foNS = OdfNamespace.newNamespace(OdfNamespaceNames.FO);
		boolean useFontFace = true;
		OdfMediaType media = OdfMediaType.getOdfMediaType(doc.getMediaType());
		if (media.equals(OdfDocument.OdfMediaType.PRESENTATION) ||
			media.equals(OdfDocument.OdfMediaType.GRAPHICS))
			useFontFace = false;
		// check that there is a font-face element for the font-name
		Element docElement = doc.getContentDom().getDocumentElement();
		if (useFontFace && docElement instanceof OdfElement)
		{
			OdfOfficeFontFaceDecls faceDecls = OdfElement.findFirstChildNode(OdfOfficeFontFaceDecls.class, (OdfElement)docElement);
			OdfStyleFontFace face = OdfElement.findFirstChildNode(OdfStyleFontFace.class, faceDecls);
			while (face != null)
			{
				if (face.getSvgFontFamilyAttribute().equals(converter.getNewStyle().getFontName()))
				{
					newFontName = face.getStyleNameAttribute();
					break;
				}
				if (face.getStyleNameAttribute().equals(newFontName))
				{
					// same name, different font!
					newFontName = converter.getNewStyle().getFontName() + 
						(++faceCount);
				}
				face = OdfElement.findNextChildNode(OdfStyleFontFace.class, face);
			}
			if (face == null)
			{
				face = new OdfStyleFontFace(doc.getContentDom());
				face.setOdfAttributeValue(OdfName.newName(styleNS,"name"),
						newFontName);
				face.setOdfAttributeValue(OdfName.newName(svgNS,"font-family"),
						converter.getNewStyle().getFontName());
				faceDecls.appendChild(face);
			}
		}
		// set font name
		if (useFontFace)
		{
			if (type.equals(Type.LATIN))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, "font-name"), newFontName);
			}
			else if (type.equals(Type.COMPLEX))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, "font-name-complex"), newFontName);
			}
			else if (type.equals(Type.CJK))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, "font-name-asian"), newFontName);
			}
		}
		else
		{
			if (type.equals(Type.LATIN))
			{
				props.setOdfAttributeValue(OdfName.newName(foNS, "font-family"), newFontName);
			}
			else if (type.equals(Type.COMPLEX))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, "font-family-complex"), newFontName);
			}
			else if (type.equals(Type.CJK))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, "font-family-asian"), newFontName);
			}
		}
		setProperty(type, props, foNS,
				"font-size", fontSize);
		setProperty(type, props, foNS,
				"font-size-rel", fontSizeRel);
		setProperty(type, props, styleNS,
				"font-pitch", fontPitch);
		setProperty(type, props, styleNS, 
				"font-style-name", fontStyleName);
		setProperty(type, props, styleNS, 
				"font-family-generic", fontFamilyGeneric);
		setProperty(type, props, foNS,
				"font-style", fontStyle);
		setProperty(type, props, foNS,
				"font-weight", fontWeight);

		if (style.getParentNode() != autoStyles)
		{
			autoStyles.appendChild(style);
		}
		
		mConvertedStyles.get(styleName).put(converter, name);
		//System.out.println("Orig:" + styleName + ">" + name);
		return name;
	}
	
	private String parseProperty(CharConverter converter, StyleTextPropertiesElement oldProps,
			OdfNamespace ns, String attrib, String value)
	{
		if (value.length() > 0) return value;
		OdfNamespace styleNS = OdfNamespace.newNamespace(OdfNamespaceNames.STYLE);
		if (converter.getOldStyle().getScriptType().equals(Type.LATIN))
			value = oldProps.getOdfAttributeValue(OdfName.newName(ns, attrib));
		else if (converter.getOldStyle().getScriptType().equals(Type.COMPLEX))
			value = oldProps.getOdfAttributeValue(OdfName.newName(styleNS, 
					attrib + "-complex"));
		else if (converter.getOldStyle().getScriptType().equals(Type.CJK))
			value = oldProps.getOdfAttributeValue(OdfName.newName(styleNS,
					attrib + "-asian"));
		return value;
	}
	
	private void setProperty(Type type, StyleTextPropertiesElement props, OdfNamespace ns, String attrib, String value)
	{
		OdfNamespace styleNS = OdfNamespace.newNamespace(OdfNamespaceNames.STYLE);
		if (value.length() > 0)
		{
			if (type.equals(Type.LATIN))
			{
				props.setOdfAttributeValue(OdfName.newName(ns, attrib), value);
			}
			else if (type.equals(Type.COMPLEX))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, attrib +
						"-complex"), value);
			}
			else if (type.equals(Type.CJK))
			{
				props.setOdfAttributeValue(OdfName.newName(styleNS, attrib +
						"-asian"), value);
			}
		}
	}

	private ConvertibleTextFragment parseNodes(OdfDocument doc, OdfElement element, 
				Map<TextStyle, CharConverter> converters,
				Deque<OdfStyleBase> styleStack,
				Deque <ConvertibleTextFragment> textFrags,
				ConvertibleTextFragment prevText)
	{
		NodeList paraChildren = element.getChildNodes();
		for (int i = 0; i < element.getLength(); i++)
		{
			Node n = paraChildren.item(i);
			if (n instanceof OdfTextParagraph)
			{
				// set the previous text to null between paragraphs - essential for performance
				prevText = null;
			}
			if (n instanceof OdfStylableElement)
			{
				OdfStylableElement stylable = (OdfStylableElement)n;
				OdfStyleBase autoStyle = stylable.getAutomaticStyle();
				if (autoStyle == null)
				{
					// it's a real style
					autoStyle = doc.getOrCreateDocumentStyles().getStyle(stylable.getStyleName(), stylable.getStyleFamily());
				}
				if (autoStyle != null) styleStack.push(autoStyle);
				prevText = parseNodes(doc, stylable, converters, styleStack, textFrags, prevText);
				if (autoStyle != null) styleStack.pop();
				//compareStyles(paraAutoStyle, autoStyle, ScriptType.Type.LATIN);
			}
			else if (n instanceof OdfElement)
			{
				OdfElement childElement = (OdfElement)n;
				if (childElement instanceof OdfTextTab ||
					childElement instanceof OdfTextSpace)
				{
					prevText = null;
				}
				if (childElement.hasChildNodes())
				{
					prevText = parseNodes(doc, childElement, converters, styleStack, textFrags, prevText);
				}
			}
			else if (n.getNodeType() == Node.TEXT_NODE)
			{
				Text text = (Text)n;
				String textData = text.getData();
				// don't process whitespace only nodes
				if (sWhitespace.matcher(textData).matches())
					continue;
				ScriptSegment scriptSeg = ScriptType.find(textData.toCharArray(), 0, textData.length());
				while (scriptSeg != null)
				{
					CharConverter conv = getConverter(doc, styleStack, converters, scriptSeg.getType());
					if (conv == null)
					{
						prevText = null;
					}
					else
					{
						ConvertibleTextFragment ctf = new ConvertibleTextFragment(text, scriptSeg.getStart(), scriptSeg.getLength(), conv, prevText);
						textFrags.addLast(ctf);
						prevText = ctf;
					}
					
					if (scriptSeg.getStart() + scriptSeg.getLength() < textData.length())
						scriptSeg = ScriptType.find(textData.toCharArray(),
								scriptSeg.getStart() + scriptSeg.getLength(),
								textData.length() - scriptSeg.getStart() - scriptSeg.getLength());
					else
						break;
				}
			}
		}
		return prevText;
	}
	
	private CharConverter getConverter(OdfDocument doc, Deque<OdfStyleBase> styleStack,
			Map<TextStyle, CharConverter> converters, Type type)
	{
		CharConverter conv = null;
		boolean useFontFace = true;
		OdfMediaType media = OdfMediaType.getOdfMediaType(doc.getMediaType());
		if (media.equals(OdfDocument.OdfMediaType.PRESENTATION))
			useFontFace = false;
		if (media.equals(OdfDocument.OdfMediaType.GRAPHICS))
			useFontFace = false;
		
		for (OdfStyleBase style : styleStack)
		{
			String fontName = "";
			do
			{
				StyleTextPropertiesElement textProps = OdfElement.findFirstChildNode(StyleTextPropertiesElement.class, style);
				if (textProps != null)
				{
					if (useFontFace)
					{
						if (type.equals(Type.LATIN))
							fontName = textProps.getAttribute("style:font-name");
						else if (type.equals(Type.COMPLEX))
							fontName = textProps.getAttribute("style:font-name-complex");
						else if (type.equals(Type.CJK))
							fontName = textProps.getAttribute("style:font-name-asian");
						else
							fontName = textProps.getAttribute("style:font-name");
					}
					else
					{
						if (type.equals(Type.LATIN))
							fontName = textProps.getAttribute("fo:font-family");
						else if (type.equals(Type.COMPLEX))
							fontName = textProps.getAttribute("style:font-family-complex");
						else if (type.equals(Type.CJK))
							fontName = textProps.getAttribute("style:font-family-asian");
						else
							fontName = textProps.getAttribute("fo:font-family");
					}
				}
				style = style.getParentStyle();
			} while((fontName.length() == 0) && style != null);

			//final Pattern endsInNumber = Pattern.compile("^(.*?)\\d+");
			if (fontName != null && fontName.length() > 0)
			{
				if (useFontFace)
				{
					fontName = mFaceMap.get(fontName);
				}
//				Matcher m = endsInNumber.matcher(fontName);
//				if (m.matches())
//				{
//					altFontName = m.group(1);
//				}
				for (TextStyle convStyle : converters.keySet())
				{
					if (convStyle.getFontName().equalsIgnoreCase(fontName))
					{
						conv = converters.get(convStyle);
						break;
					}					
				}
				if (conv == null)
				{
					fontName = fontName.replaceAll("'", "");
					for (TextStyle convStyle : converters.keySet())
					{
						if (convStyle.getFontName().equalsIgnoreCase(fontName))
						{
							conv = converters.get(convStyle);
							break;
						}					
					}
				}
				break;
			}
		}
		
		return conv;
	}

	@Override
	public void setInputEncoding(Charset enc)
	{
	
	}

	@Override
	public void setMode(ConversionMode mode)
	{
		mMode = mode;
	}

	@Override
	public void setOutputEncoding(Charset enc)
	{
	
	}

}
