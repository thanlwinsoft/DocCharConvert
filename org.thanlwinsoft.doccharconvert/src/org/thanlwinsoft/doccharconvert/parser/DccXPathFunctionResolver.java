/*
Copyright (C) 2008 Keith Stribley http://www.thanlwinsoft.org/

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
package org.thanlwinsoft.doccharconvert.parser;

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.thanlwinsoft.doccharconvert.FontStyle;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;

/**
 * 
 * @author keith
 * XPath function resolver for DocCharConvert converters
 */
public class DccXPathFunctionResolver implements XPathFunctionResolver
{
    final Map<TextStyle, CharConverter> mConverters;
    DccXPathFunctionResolver(Map<TextStyle, CharConverter> converters)
    {
        mConverters = converters;
    }
    @Override
    public XPathFunction resolveFunction(QName functionName, int arity)
    {
        if (functionName.getNamespaceURI().equals("http://www.thanlwinsoft.org/doccharconvert/XPath")
            && functionName.getLocalPart().equals("convert") && arity == 1)
        {
            return new XPathFunction() {

                @Override
                @SuppressWarnings(value={"unchecked"})
                public Object evaluate(List args) throws XPathFunctionException
                {
                    CharConverter cc = mConverters.values().iterator().next();
                    if (args.size() > 1)
                        cc = mConverters.get(new FontStyle(args.get(1).toString()));
                    try
                    {
                        if (cc != null) return cc.convert(args.get(0).toString());
                    }
                    catch (FatalException e)
                    {
                        throw new XPathFunctionException(e);
                    }
                    catch (RecoverableException e)
                    {
                        throw new XPathFunctionException(e);
                    }
                    return args.get(0);
                }};
        }
        return null;
    }

}
