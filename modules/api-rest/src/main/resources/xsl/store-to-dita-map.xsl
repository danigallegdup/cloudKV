<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <map id="cloudkv">
      <title>cloudKV Topics</title>
      <xsl:for-each select="store/entry">
        <topicref href="{concat('topics/', @key, '.dita')}" keys="{@key}"/>
      </xsl:for-each>
    </map>
  </xsl:template>
</xsl:stylesheet>
