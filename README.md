# oss-compliance

Central compliance tooling for the Schwarz Digits open-source program.

This repository provides a reusable GitHub Actions workflow that performs
the standard compliance checks required for open-source repositories in
our organizations (`SchwarzDigits`, `SchwarzIT`).

## What it does

When integrated into a repository, the workflow runs on every pull
request and push to the default branch and performs:

- **Secret scanning** with Gitleaks (full git history)
- **License compliance** check against our central denylist
- **Vulnerability scanning** with Trivy (critical severities fail the build, high produces warnings)
- **SBOM generation** with Syft (SPDX and CycloneDX formats, uploaded as workflow artifact)

## Quick start

Add the following workflow to your repository at
`.github/workflows/compliance.yml`:

```yaml
name: Compliance

on:
  pull_request:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  compliance:
    uses: SchwarzDigits/oss-compliance/.github/workflows/full-check.yml@v1
```

That's it. The workflow takes care of installing the tools, fetching
the policy, and running all checks.

For more detail, see [docs/onboarding.md](docs/onboarding.md).

## Versioning

Stable, breaking-change-free integration is provided through the `v1`
git tag. Major-version increments may introduce breaking changes; minor
and patch updates are non-breaking.

## Governance

This repository is maintained by the Schwarz Digits Open Source
Committee. Changes to the workflow logic and the license denylist
require committee review.

For questions, contact
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).

## License

Apache License 2.0. See [LICENSE](LICENSE).