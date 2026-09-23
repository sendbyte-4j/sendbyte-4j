# Security Policy

Report suspected vulnerabilities privately to the SendByte SDK maintainers before opening a public issue. Include the affected artifact, version, impact, reproduction steps, and whether credentials, message content, recipients, webhook secrets, or customer data may be exposed.

Supported versions are the latest released minor line and any release candidate explicitly marked as supported. Preview modules are investigated with the same care, but fixes may require API changes before they are labeled stable.

Do not include production API keys, webhook secrets, personal data, message bodies, recipient lists, or customer identifiers in reports, fixtures, logs, screenshots, or pull requests. Use placeholders such as `sk_test_example` or `whsec_test_secret`.

The release gate runs license checks, source/configuration secret scanning, consumer dependency-boundary checks, and published-artifact content checks. Any confirmed credential or personal-data leak blocks release until rotated, removed, and documented.
