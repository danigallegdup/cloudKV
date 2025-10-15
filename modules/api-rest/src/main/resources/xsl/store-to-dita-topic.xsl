<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dita="http://dita.oasis-open.org/architecture/2005/">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <xsl:for-each select="store/entry">
      <topic id="{@key}">
        <title><xsl:value-of select="@key"/></title>
        <body>
          <p><xsl:value-of select="value"/></p>
          <xsl:if test="@ttlMs">
            <note type="other">ttlMs: <xsl:value-of select="@ttlMs"/></note>
          </xsl:if>
        </body>
      </topic>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
