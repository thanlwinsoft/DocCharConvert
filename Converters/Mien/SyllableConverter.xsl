<?xml version="1.0" encoding="UTF-8"?>

<!--
    $HeadURL: $
    $LastChangedDate: $
    $LastChangedRevision: $

    This is the XSLT file to transform the OSIS bible XML format into
    HTML for viewing in a web browser.
    
    Copyright (C) 2004, 2005 Myanmar Bible Society <tech@myanmarbible.com>

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
  
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
  
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

Description:
  Stylesheet to create a list of the book titles and Testament titles from
  the OSIS source.    
    
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sp="http://www.thanlwinsoft.org/schemas/SyllableParser"
xmlns="http://www.w3.org/1999/xhtml"
>

<xsl:output method="xml"/>

<xsl:variable name="title">
<xsl:text>SyllableParser: </xsl:text>
<xsl:value-of select="/sp:syllableConverter/sp:script[@side='left']/sp:name"/>
<xsl:text>/</xsl:text>
<xsl:value-of select="/sp:syllableConverter/sp:script[@side='right']/sp:name"/>
</xsl:variable>

<xsl:template match="/">
<html>
<head>
<meta content="text/html; charset=UTF-8" http-equiv="content-type" />
<title><xsl:value-of select="$title"/></title>
<style type="text/css">
h1, h2, h3 {
padding-left: 0.2em;

border-left-color: #000000;
border-left-style: solid;
border-left-width: 0.2em;

border-top-color: #000000;
border-top-style: solid;
border-top-width: 4px;

border-bottom-color: #000000;
border-bottom-style: solid;
border-bottom-width: 2px;

background-color: #ccccff;
}
h2 {
border-top-width: 2px;
border-bottom-width: 1px;
}
h3{
border-top-width: 1px;
border-bottom-width: 0px;
}
table {
  width: 100%;
  border-width: 2px;
  border-style: solid;
  padding: 0px;
  border-collapse: collapse;
}
tr {
  padding: 0px;
}
th.rowTitle {
  text-align: left;
  border-style: none;
  border-width: 0px;
}
td, th {
  border-width: 0px;
  border-left-style: solid;
  border-left-width: 1px;
  text-align: center;
}
.hexCode {
}
.classRef {
  font-style: italic;
}
th.left {
  background-color: #ffaaaa;
}
th.right {
  background-color: #aaffaa;
}
.left {
  background-color: #ffcccc;
}
.right {
  background-color: #ccffcc;
}
hr {
  border-style: solid;
  border-color: black;
  border-width: 0px;
  border-top-width: 2px;
}
</style>
<script type="text/javascript">
function fixHex()
{
  var nodes = document.getElementsByTagName('span');
  for (var i = 0; i &lt; nodes.length; i++)
  {
    var node = nodes.item(i);
    if (node.hasAttribute('hexValue'))
    {
      node.title = node.getAttribute('hexValue');
      var hexCode = node.innerHTML;
      if (node.hasAttribute('decValue'))
      {
        try {
          var newValue = String.fromCharCode(node.getAttribute('decValue'));
          if (node.innerHTML) node.innerHTML = newValue;
        }
        catch (e) { node.innerHTML = hexCode; }
      }
    }
  }
}
</script>
</head>
<body onload="fixHex()">
<h1><xsl:value-of select="$title"/></h1>
<h2 id="contents">Contents</h2>
<ul>
<li><a href="#leftComponents">Left Components</a></li>
<li><a href="#rightComponents">Right Components</a></li>
<li><a href="#checkers">Syllable Checkers</a></li>
<li><a href="#repeats">Repeating syllables</a></li>
<li><a href="#classes">Classes</a>
<ul>
<xsl:for-each select="/sp:syllableConverter/sp:classes/sp:class">
<li><a href="#class_{@id}"><xsl:value-of select="@id"/></a></li>
</xsl:for-each>
</ul></li>
<li><a href="#tables">Tables</a>
<ul>
<xsl:for-each select="/sp:syllableConverter/sp:mappingTable">
<li><a href="#mapTable_{@id}"><xsl:value-of select="@id"/></a></li>
</xsl:for-each>
</ul></li>
</ul>

<xsl:for-each select="/sp:syllableConverter/sp:script">
<h2><xsl:element name="a">
<xsl:attribute name="id"><xsl:value-of select="@side"/>Components</xsl:attribute>
<a href="#contents">
<xsl:value-of select="sp:name"/>: syllables on <xsl:value-of select="@side"/> side
</a>
</xsl:element>
</h2>
<xsl:variable name="side" select="@side"/>
<table>
<tr><th class="rowTitle">ID:</th>
<xsl:for-each select="sp:cluster/sp:component">
<th class="{$side}">
<xsl:element name="a">
<xsl:attribute name="id">
<xsl:value-of select="concat('component_',@id)"/>
</xsl:attribute>
<xsl:value-of select="@id"/>
</xsl:element>
</th>
</xsl:for-each>
</tr>
<tr><th class="rowTitle">Name:</th>
<xsl:for-each select="sp:cluster/sp:component">
<td class="{$side}"><xsl:value-of select="./text()"/></td>
</xsl:for-each>
</tr>
<tr><th class="rowTitle">Priority:</th>
<xsl:for-each select="sp:cluster/sp:component">
<td class="{$side}"><xsl:value-of select="@priority"/></td>
</xsl:for-each>
</tr>
<tr><th class="rowTitle">Min:</th>
<xsl:for-each select="sp:cluster/sp:component">
<td class="{$side}"><xsl:value-of select="@min"/></td>
</xsl:for-each>
</tr>
</table>
</xsl:for-each>

<h2 id="checkers"><a href="#contents">Syllable Checks</a></h2>
<xsl:apply-templates select="/sp:syllableConverter/sp:checks"/>
<h2 id="repeats"><a href="#contents">Repeating Syllable Markers</a></h2>
<xsl:apply-templates select="/sp:syllableConverter/sp:repeat"/>

<h2 id="classes"><a href="#contents">Classes</a></h2>
<xsl:apply-templates select="/sp:syllableConverter/sp:classes"/>
<h2 id="tables"><a href="#contents">Mapping Tables</a></h2>
<xsl:apply-templates select="/sp:syllableConverter/sp:mappingTable"/>

<hr/>
<p><i>Generated directly from the configuration file using <tt>SyllableConverter.xsl</tt>.</i></p>
</body>
</html>
</xsl:template>

<xsl:template match="sp:checks">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="sp:checker">
<tt><xsl:value-of select="@class"/></tt>
(<xsl:apply-templates/>)<br/>
</xsl:template>

<xsl:template match="sp:arg">
<sup><xsl:value-of select="@type"/></sup>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="repeat">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="sp:marker">
<p class="{@side}"><xsl:text>Repeat Marker (</xsl:text>
<xsl:value-of select="@side"/><xsl:text>):</xsl:text>
<xsl:if test="@hex">
<xsl:call-template name="hex">
<xsl:with-param name="hexCode" select="@hex"/>
</xsl:call-template>
</xsl:if>
<xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="sp:separator">
<p class="{@side}"><xsl:text>Syllable separator (</xsl:text>
<xsl:value-of select="@side"/><xsl:text>):</xsl:text>
<xsl:if test="@hex">
<xsl:call-template name="hex">
<xsl:with-param name="hexCode" select="@hex"/>
</xsl:call-template>
</xsl:if>
<xsl:value-of select="."/></p>
</xsl:template>

<xsl:template name="hex">
<xsl:param name="hexCode" select="0020"/>
<!-- This code successfully gets the decimal value, but I'm not sure how to make it into an entity! -->

<xsl:variable name="hs4" select="substring($hexCode,1,1)"/>
<xsl:variable name="hs3" select="substring($hexCode,2,1)"/>
<xsl:variable name="hs2" select="substring($hexCode,3,1)"/>
<xsl:variable name="hs1" select="substring($hexCode,4,1)"/>
<xsl:variable name="h4"
 select="number(concat(translate($hs4,'0123456789abcdefABCDEF','0000000000111111111111'),
                       translate($hs4,'0123456789abcdefABCDEF','0123456789012345012345')))"   
        />
<xsl:variable name="h3"
 select="number(concat(translate($hs3,'0123456789abcdefABCDEF','0000000000111111111111'),
                      translate($hs3,'0123456789abcdefABCDEF','0123456789012345012345')))"
                      />
<xsl:variable name="h2"
 select="number(concat(translate($hs2,'0123456789abcdefABCDEF','0000000000111111111111'),
                      translate($hs2,'0123456789abcdefABCDEF','0123456789012345012345')))"
                      />
<xsl:variable name="h1"
 select="number(concat(translate($hs1,'0123456789abcdefABCDEF','0000000000111111111111'),
                      translate($hs1,'0123456789abcdefABCDEF','0123456789012345012345')))"
                      />
<xsl:variable name="decValue" select="$h4*4096+$h3*256+$h2*16+$h1"/>
<!--<span class="hexCode" onmouseover="this.title='\u{substring($hexCode,1,4)}'">-->
<span class="hexCode" hexValue='{substring($hexCode,1,4)}'
  decValue="{$decValue}">
<xsl:value-of select="substring($hexCode,1,4)"/><xsl:text> </xsl:text>
</span>
<!--
<xsl:text><![CDATA[&]]></xsl:text>
<xsl:text>#</xsl:text><xsl:value-of select="$decValue"/><xsl:text>;</xsl:text>
-->

<xsl:if test="string-length($hexCode)&gt;4">
<xsl:call-template name="hex">
<xsl:with-param name="hexCode" select="substring($hexCode,6)"/>
</xsl:call-template>
</xsl:if>
</xsl:template>

<xsl:template match="sp:class">
<h3><a id="{concat('class_',@id)}"><a href="#contents"><xsl:value-of select="@id"/></a></a></h3>
<table>
<xsl:apply-templates/>
</table>
</xsl:template>

<xsl:template match="sp:component">
<xsl:variable name="cId" select="@r"/>
<xsl:variable name="side">
<xsl:choose>
<xsl:when
 test="/sp:syllableConverter/sp:script[@side='left']/sp:cluster/sp:component[@id=$cId]">
left
</xsl:when>
<xsl:otherwise>right</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<tr><th class="{$side}"><a href="#component_{@r}"><xsl:value-of select="@r"/></a></th>
<xsl:apply-templates/>
</tr>
</xsl:template>

<xsl:template match="sp:mappingTable">
<h3><a id="mapTable_{@id}"><a href="#contents"><xsl:value-of select="@id"/></a></a></h3>
<xsl:if test="@optional">
<p>Optional:<xsl:value-of select="@optional"/></p>
</xsl:if>
<table>
<tr>
<xsl:for-each select="sp:columns/sp:component">
<xsl:variable name="cId" select="@r"/>
<xsl:variable name="side">
<xsl:choose>
<xsl:when
 test="/sp:syllableConverter/sp:script[@side='left']/sp:cluster/sp:component[@id=$cId]">
left
</xsl:when>
<xsl:otherwise>right</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<th class="{$side}"><a href="#component_{@r}"><xsl:value-of select="@r"/></a></th>
</xsl:for-each>
</tr>
<xsl:for-each select="sp:maps/sp:m">
<tr><xsl:apply-templates/></tr>
</xsl:for-each>
</table>
</xsl:template>

<xsl:template match="sp:c">
<xsl:variable name="cId">
<xsl:choose>
<xsl:when test="@r">
<xsl:value-of select="@r"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="../@r"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<!--<xsl:variable side="//sp:component[@id=$cId]/parent()/@side"/>-->
<xsl:variable name="side">
<xsl:choose>
<xsl:when test="/sp:syllableConverter/sp:script[@side='left']/sp:cluster/sp:component[@id=$cId]">
left
</xsl:when>
<xsl:otherwise>right</xsl:otherwise>
</xsl:choose>
</xsl:variable>
<td class="{$side}">
<xsl:choose>
<xsl:when test="@hex">
<xsl:call-template name="hex">
<xsl:with-param name="hexCode" select="@hex"/>
</xsl:call-template>
</xsl:when>
<xsl:when test="@class">
<a href="#class_{@class}" class="classRef"><xsl:value-of select="@class"/></a>
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates/>
</xsl:otherwise>
</xsl:choose>
</td>
</xsl:template>

</xsl:stylesheet>

