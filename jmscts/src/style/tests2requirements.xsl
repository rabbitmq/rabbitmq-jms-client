<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="requirements"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="doc" select="document($requirements)"/>

  <xsl:template match="/">
    <document>
      <properties>
        <title>Test Cases</title>
      </properties>
      <body>
        <xsl:apply-templates select="meta-data"/>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="meta-data">
    <xsl:apply-templates 
         select="class-meta[method-meta/attribute/@name='jmscts.requirement']">
      <xsl:sort select="name"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="class-meta">
    <p/>
    <xsl:element name="section">
      <xsl:attribute name="name">
        <xsl:value-of select="name" />
      </xsl:attribute>
      <p>
        <xsl:copy-of select="description/node()"/>
      </p>      
      <xsl:apply-templates 
           select="method-meta[attribute/@name='jmscts.requirement']">
        <xsl:sort select="name"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>

  <xsl:template match="method-meta">
    <xsl:element name="subsection">
      <xsl:attribute name="name">
        <xsl:value-of select="name" />
      </xsl:attribute>
      <p>
        <xsl:copy-of select="description/node()"/>
      </p>      
      <table>
        <th>Requirements</th>
        <xsl:apply-templates select="attribute[@name='jmscts.requirement']"/>
      </table>
    </xsl:element>
  </xsl:template>

  <xsl:template match="attribute[@name='jmscts.requirement']">
    <xsl:variable name="reqId" select="@value"/>
    <tr>    
      <td><xsl:value-of select="$reqId"/></td>
      <td>
        <xsl:copy-of select="$doc/document/requirement[@requirementId=$reqId]/description/node()"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
