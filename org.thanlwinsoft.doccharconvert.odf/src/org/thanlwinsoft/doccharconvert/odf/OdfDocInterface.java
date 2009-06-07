package org.thanlwinsoft.doccharconvert.odf;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;


import org.openoffice.odf.doc.OdfDocument;
import org.openoffice.odf.doc.OdfFileDom;
import org.openoffice.odf.doc.element.office.OdfAutomaticStyles;
import org.openoffice.odf.doc.element.office.OdfDrawing;
import org.openoffice.odf.doc.element.office.OdfFontFaceDecls;
import org.openoffice.odf.doc.element.office.OdfPresentation;
import org.openoffice.odf.doc.element.office.OdfSpreadsheet;
import org.openoffice.odf.doc.element.office.OdfStyles;
import org.openoffice.odf.doc.element.office.OdfText;
import org.openoffice.odf.doc.element.office.OdfBody;
import org.openoffice.odf.doc.element.style.OdfFontFace;
import org.openoffice.odf.doc.element.style.OdfStyle;
import org.openoffice.odf.doc.element.style.OdfTextProperties;
import org.openoffice.odf.doc.element.text.OdfParagraph;
import org.openoffice.odf.doc.element.text.OdfSpace;
import org.openoffice.odf.doc.element.text.OdfSpan;
import org.openoffice.odf.doc.element.text.OdfTab;
import org.openoffice.odf.dom.OdfName;
import org.openoffice.odf.dom.OdfNamespace;
import org.openoffice.odf.dom.element.OdfElement;
import org.openoffice.odf.dom.element.OdfStylableElement;
import org.openoffice.odf.dom.element.OdfStyleBase;
import org.openoffice.odf.dom.element.style.OdfStyleElement;
import org.openoffice.odf.dom.element.text.OdfParagraphElementBase;
import org.openoffice.odf.dom.style.OdfStyleFamily;
import org.openoffice.odf.dom.style.props.OdfStylePropertiesSet;
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
 * @author keith
 * Document Converter Interface using ODF DOM
 */
public class OdfDocInterface implements DocInterface {

	private HashMap<String, HashMap<CharConverter, String> > mConvertedStyles = new HashMap<String, HashMap<CharConverter, String> >(); 
	private int mTextStyleCount = 0;
	@Override
	public void abort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public ConversionMode getMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusDesc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialise() throws InterfaceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void parse(File input, File output,
			Map<TextStyle, CharConverter> converters, ProgressNotifier notifier)
			throws FatalException, InterfaceException, WarningException
	{
		try
		{
			OdfDocument odfInput = OdfDocument.loadDocument(input);
			OdfBody body = odfInput.getOfficeBody();
			
			Deque <OdfStyleBase> styleStack = new ArrayDeque<OdfStyleBase>();
			styleStack.push(odfInput.getDocumentStyles().getDefaultStyle(OdfStyleFamily.Paragraph));
			
			Deque <ConvertibleTextFragment> textFrags = new ArrayDeque <ConvertibleTextFragment>();
			ConvertibleTextFragment prevText = null;

			parseNodes(odfInput, body, converters, styleStack, textFrags, prevText);
			convertNodes(odfInput, body, textFrags);
			
			odfInput.save(output);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new FatalException(e.getLocalizedMessage());
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
							if (combined.equals(converted + nextConverted) == true)
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
				if (parent instanceof OdfSpan)
				{
					// easy case - whole of text span
					if (parent.getChildNodes().getLength() == 1 &&
						frag.getStart() == 0 &&
						frag.getLength() == frag.getNode().getLength())
					{
						OdfSpan span = (OdfSpan)parent;
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
						OdfSpan span = (OdfSpan)parent;
						OdfSpan convertedSpan = new OdfSpan(dom);
						convertedSpan.setStyleName(getConvertedStyle(doc, 
								frag.getConverter(), span.getStyleName()));
						
						OdfSpan postSpan = null;
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
							postSpan = new OdfSpan(dom);
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
					OdfSpan convertedSpan = new OdfSpan(dom);
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
	
	private OdfElement getTopLevelParent(Node n)
	{
		Node paraLevelParent = n;
		while (paraLevelParent != null &&
			   !(paraLevelParent.getParentNode() instanceof OdfText) &&
			   !(paraLevelParent.getParentNode() instanceof OdfPresentation) &&
			   !(paraLevelParent.getParentNode() instanceof OdfDrawing) &&
			   !(paraLevelParent.getParentNode() instanceof OdfSpreadsheet))
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
				return convertedStyle;
			}

		}
		else
		{
			mConvertedStyles.put(styleName, new HashMap<CharConverter,String>());
		}
		// need to create a new style
		//OdfStyles styles = doc.getOrCreateDocumentStyles();
		OdfAutomaticStyles autoStyles = doc.getContentDom().getAutomaticStyles();
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
		String fontSize = "";
		String fontSizeRel = "";
		String fontPitch = "";
		String fontStyleName = "";
		String fontFamilyGeneric = "";
		String fontStyle = "";
		String fontWeight = "";
		while (origStyle != null)
		{
			OdfTextProperties oldProps = (OdfTextProperties)origStyle.getOrCreatePropertiesElement(OdfStylePropertiesSet.TextProperties);
			fontSize = parseProperty(converter, oldProps, OdfNamespace.FO,
					"font-size", fontSize);
			fontSizeRel = parseProperty(converter, oldProps, OdfNamespace.FO,
					"font-size-rel", fontSizeRel);
			fontPitch = parseProperty(converter, oldProps, OdfNamespace.STYLE, 
					"font-pitch", fontPitch);
			fontStyleName = parseProperty(converter, oldProps, OdfNamespace.STYLE, 
					"font-style-name", fontStyleName);
			fontFamilyGeneric = parseProperty(converter, oldProps, OdfNamespace.STYLE, 
					"font-family-generic", fontFamilyGeneric);
			fontStyle = parseProperty(converter, oldProps, OdfNamespace.FO, 
					"font-style", fontStyle);
			fontWeight = parseProperty(converter, oldProps, OdfNamespace.FO, 
					"font-weight", fontWeight);
			
			origStyle = origStyle.getParentStyle();
		}
		
		OdfStyleElement style = autoStyles.createStyle(OdfStyleFamily.Text);
		style.setParentStyleName(styleName);
		style.setName(name);
		OdfTextProperties props = (OdfTextProperties) style.getOrCreatePropertiesElement(OdfStylePropertiesSet.TextProperties);
		// TODO support font-family
		Type type = converter.getNewStyle().getScriptType();
		String newFontName = converter.getNewStyle().getFontName();
		int faceCount = 0;

		// check that there is a font-face element for the font-name
		Element docElement = doc.getContentDom().getDocumentElement();
		if (docElement instanceof OdfElement)
		{
			OdfFontFaceDecls faceDecls = OdfElement.findFirstChildNode(OdfFontFaceDecls.class, (OdfElement)docElement);
			OdfFontFace face = OdfElement.findFirstChildNode(OdfFontFace.class, faceDecls);
			while (face != null)
			{
				if (face.getFontFamily().equals(converter.getNewStyle().getFontName()))
				{
					newFontName = face.getName();
					break;
				}
				if (face.getName().equals(newFontName))
				{
					// same name, different font!
					newFontName = converter.getNewStyle().getFontName() + 
						(++faceCount);
				}
				face = OdfElement.findNextChildNode(OdfFontFace.class, face);
			}
			if (face == null)
			{
				face = new OdfFontFace(doc.getContentDom());
				face.setOdfAttribute(OdfName.get(OdfNamespace.STYLE,"name"),
						newFontName);
				face.setOdfAttribute(OdfName.get(OdfNamespace.SVG,"font-family"),
						converter.getNewStyle().getFontName());
				faceDecls.appendChild(face);
			}
		}
		// set font name
		if (type.equals(Type.LATIN))
		{
			props.setOdfAttribute(OdfName.get(OdfNamespace.FO, "font-name"), newFontName);
		}
		else if (type.equals(Type.COMPLEX))
		{
			props.setOdfAttribute(OdfName.get(OdfNamespace.STYLE, "font-name-complex"), newFontName);
		}
		else if (type.equals(Type.CJK))
		{
			props.setOdfAttribute(OdfName.get(OdfNamespace.STYLE, "font-name-asian"), newFontName);
		}
		setProperty(type, props, OdfNamespace.FO,
				"font-size", fontSize);
		setProperty(type, props, OdfNamespace.FO,
				"font-size-rel", fontSizeRel);
		setProperty(type, props, OdfNamespace.STYLE,
				"font-pitch", fontPitch);
		setProperty(type, props, OdfNamespace.STYLE, 
				"font-style-name", fontStyleName);
		setProperty(type, props, OdfNamespace.STYLE, 
				"font-family-generic", fontFamilyGeneric);
		setProperty(type, props, OdfNamespace.FO,
				"font-style", fontStyle);
		setProperty(type, props, OdfNamespace.FO,
				"font-weight", fontWeight);

		
		
		mConvertedStyles.get(styleName).put(converter, name);
		return name;
	}
	
	private String parseProperty(CharConverter converter, OdfTextProperties oldProps,
			OdfNamespace ns, String attrib, String value)
	{
		if (value.length() > 0) return value;
		if (converter.getOldStyle().getScriptType().equals(Type.LATIN))
			value = oldProps.getOdfAttribute(OdfName.get(ns, attrib));
		else if (converter.getOldStyle().getScriptType().equals(Type.COMPLEX))
			value = oldProps.getOdfAttribute(OdfName.get(OdfNamespace.STYLE, 
					attrib + "-complex"));
		else if (converter.getOldStyle().getScriptType().equals(Type.CJK))
			value = oldProps.getOdfAttribute(OdfName.get(OdfNamespace.STYLE,
					attrib + "-asian"));
		return value;
	}
	
	private void setProperty(Type type, OdfTextProperties props, OdfNamespace ns, String attrib, String value)
	{
		if (value.length() > 0)
		{
			if (type.equals(Type.LATIN))
			{
				props.setOdfAttribute(OdfName.get(ns, attrib), value);
			}
			else if (type.equals(Type.COMPLEX))
			{
				props.setOdfAttribute(OdfName.get(OdfNamespace.STYLE, attrib +
						"-complex"), value);
			}
			else if (type.equals(Type.CJK))
			{
				props.setOdfAttribute(OdfName.get(OdfNamespace.STYLE, attrib +
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
				if (childElement instanceof OdfTab ||
					childElement instanceof OdfSpace)
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
				ScriptSegment scriptSeg = ScriptType.find(textData.toCharArray(), 0, textData.length());
				while (scriptSeg != null)
				{
					CharConverter conv = getConverter(styleStack, converters, scriptSeg.getType());
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
	
	private CharConverter getConverter(Deque<OdfStyleBase> styleStack,
			Map<TextStyle, CharConverter> converters, Type type)
	{
		CharConverter conv = null;
		for (OdfStyleBase style : styleStack)
		{
			String fontName = "";
			do
			{
				OdfTextProperties textProps = OdfElement.findFirstChildNode(OdfTextProperties.class, style);
				if (textProps != null)
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
				style = style.getParentStyle();
			} while((fontName.length() == 0) && style != null);
			if (fontName != null && fontName.length() > 0)
			{
				for (TextStyle convStyle : converters.keySet())
				{
					if (convStyle.getFontName().equalsIgnoreCase(fontName))
					{
						conv = converters.get(convStyle);
						break;
					}	
				}
				break;
			}
		}
		
		return conv;
	}

	private int compareStyles(OdfStyleBase a, OdfStyleBase b, ScriptType.Type type)
	{
		if (a == null)
		{
			if (b == null) return 0;
			else return -1;
		}
		else if (b == null)
		{
			return 1;
		}
		OdfTextProperties aProps = OdfElement.findFirstChildNode(OdfTextProperties.class, a);
		OdfTextProperties bProps = OdfElement.findFirstChildNode(OdfTextProperties.class, b);
		if (aProps == null && bProps == null) return 0;
		if (aProps == null) return -1;
		if (bProps == null) return 1;
		String aFontSize = null;
		String bFontSize = null;
		String aFontName = null;
		String bFontName = null;
		String aFontWeight = null;
		String bFontWeight = null;
		if (type == ScriptType.Type.WEAK || type == ScriptType.Type.LATIN)
		{
			aFontName = aProps.getAttribute("style:font-name");
			bFontName = bProps.getAttribute("style:font-name");
			aFontSize = aProps.getAttribute("fo:font-size");
			bFontSize = bProps.getAttribute("fo:font-size");
			aFontWeight = aProps.getAttribute("fo:font-weight");
			bFontWeight = bProps.getAttribute("fo:font-weight");
		}
		else if (type == ScriptType.Type.COMPLEX)
		{
			aFontName = aProps.getAttribute("style:font-name-complex");
			bFontName = bProps.getAttribute("style:font-name-complex");
			aFontSize = aProps.getAttribute("style:font-size-complex");
			bFontSize = bProps.getAttribute("style:font-size-complex");
			aFontWeight = aProps.getAttribute("style:font-weight-complex");
			bFontWeight = bProps.getAttribute("style:font-weight-complex");
		}
		else if (type == ScriptType.Type.CJK)
		{
			aFontName = aProps.getAttribute("style:font-name-asian");
			bFontName = bProps.getAttribute("style:font-name-asian");
			aFontSize = aProps.getAttribute("style:font-size-asian");
			bFontSize = bProps.getAttribute("style:font-size-asian");
			aFontWeight = aProps.getAttribute("style:font-weight-asian");
			bFontWeight = bProps.getAttribute("style:font-weight-asian");
		}
		int result = 0;
		if (aFontName == null && bFontName == null)
		{
			result = 0;
		}
		else if (aFontName != null)
		{
			result = aFontName.compareTo(bFontName);
		}
		else
		{
			result = -1;
		}
		if (result != 0) return result;
		if (aFontSize == null && bFontSize == null)
		{
			result = 0;
		}
		else if (aFontSize != null)
		{
			result = aFontSize.compareTo(bFontSize);
		}
		else
		{
			result = -1;
		}
		if (result != 0) return result;
		if (aFontWeight == null && bFontWeight == null)
		{
			result = 0;
		}
		else if (aFontWeight != null)
		{
			result = aFontWeight.compareTo(bFontWeight);
		}
		else
		{
			result = -1;
		}
		if (result != 0) return result;
		return 0;
	}

	@Override
	public void setInputEncoding(Charset enc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMode(ConversionMode mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutputEncoding(Charset enc) {
		// TODO Auto-generated method stub

	}

}
