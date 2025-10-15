# ADR: XML export + XSLT pipeline

Status: Proposed

Context
-------
We want a simple, dependency-light way to export the in-memory key-value store as XML and provide an HTML user-friendly view produced from XSLT. This will enable snapshots, debugging, and simple exports without adding heavy persistence.

Decision
--------
- Add an `ExportService` in the `api-rest` module that can produce a DOM snapshot of the store.
- Use Saxon-HE for XSLT 3.0 transformations (s9api compatible).
- Add endpoints `/export/xml` (application/xml) and `/export/html` (text/html).
- Feature flag the export via `XML_EXPORT_ENABLED` env var or system property (defaults on in development).

Consequences
------------
- Adds a runtime dependency on Saxon-HE.
- Snapshot currently iterates the `ConcurrentKVStore.raw()` map; for extensibility add a `forEach` method on `KVStore` later.
- Integration tests will cover both XML and HTML outputs.
