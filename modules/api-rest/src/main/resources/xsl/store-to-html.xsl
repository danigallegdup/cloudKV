<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes"/>
  <xsl:template match="/">
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>cloudKV Snapshot</title>
        <style>table{border-collapse:collapse}td,th{padding:6px;border:1px solid #ccc}</style>
      </head>
      <body>
        <h1>cloudKV Snapshot</h1>
        <table>
          <tr><th>Key</th><th>Value</th><th>ttlMs</th></tr>
          <xsl:for-each select="store/entry">
            <tr>
              <td><xsl:value-of select="@key"/></td>
              <td><xsl:value-of select="value"/></td>
              <td><xsl:value-of select="@ttlMs"/></td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
