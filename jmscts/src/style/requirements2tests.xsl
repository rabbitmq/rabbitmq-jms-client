<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="metadata"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:variable name="doc" select="document($metadata)"/>

  <xsl:template match="/">
    <document>
      <properties>
        <title>Requirements</title>
      </properties>
      <body>
        <xsl:apply-templates select="document"/>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="document">
    <section name="Requirements">
      <p>
        <xsl:apply-templates select="requirement"/>
      </p>
    </section>
  </xsl:template>

  <xsl:template match="requirement">
    <xsl:element name="subsection">
      <xsl:variable name="reqId" select="@requirementId"/>
      <xsl:attribute name="name">
        <xsl:value-of select="$reqId"/>
      </xsl:attribute>
      <p>
        <xsl:copy-of select="description/node()"/>
      </p>
      <p>
        See:
        <ul>
          <xsl:apply-templates select="referenceId"/>
          <xsl:apply-templates select="reference"/>
        </ul>
      </p>
      <p>
        Test cases:
        <ul>
          <xsl:for-each 
               select="$doc//method-meta/attribute[@name='jmscts.requirement' 
                       and @value=$reqId]">
              <xsl:variable name="class" select="../../name"/>
              <xsl:variable name="method" select="../name"/>
              <li>
                <xsl:value-of select="concat($class,'.',$method)"/>
              </li>
          </xsl:for-each>
        </ul>
      </p>
    </xsl:element>
  </xsl:template>

  <xsl:template match="referenceId">
    <xsl:variable name="id" select="." />
    <xsl:apply-templates 
         select="ancestor::document/reference[@referenceId=$id]"/>
  </xsl:template>

  <xsl:template match="reference">
    <li>
      <xsl:choose>
        <xsl:when test="section">
          Section&#160;<xsl:value-of select="section/@name" />,         
          <xsl:value-of select="section/@title" />   
        </xsl:when>
        <xsl:when test="table">
          Table <xsl:value-of select="table" />
        </xsl:when>
        <xsl:when test="url">
          <xsl:element name="a">
            <xsl:attribute name="href">
              <xsl:value-of select="url" />
            </xsl:attribute>
            <xsl:value-of select="url" />
          </xsl:element>     
        </xsl:when>
      </xsl:choose>
    </li>
  </xsl:template>

</xsl:stylesheet>
