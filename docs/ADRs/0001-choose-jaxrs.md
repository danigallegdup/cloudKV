# ADR 0001 — Choose JAX-RS (Jersey) on Jetty

- **Context:** Need a standards-based REST layer that plays well with Java web servers and filters/mappers.
- **Decision:** Use **JAX-RS (Jersey)** with **Jetty** runtime.
- **Status:** Accepted.
- **Consequences:**
  - Pros: Standard annotations, clean exception mappers, container filters, aligns with enterprise stacks (Bluestream).
  - Cons: Slightly more setup than minimalist microframeworks.
  - Alternatives considered: SparkJava (lighter but non-standard), Spring MVC (heavier for this demo).
