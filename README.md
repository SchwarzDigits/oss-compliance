# oss-compliance

Central compliance tooling for the Schwarz Digits open-source program.

This repository provides a reusable GitHub Actions workflow that performs
the standard compliance checks required for open-source repositories in
our organizations (`SchwarzDigits`, `SchwarzIT`).

It also serves as the central ORT configuration repository, hosting the
license classifications and evaluator rules that ORT applies during the
license analysis.

## What it does

When integrated into a repository, the workflow runs on every pull
request and push to the default branch and performs:

- **Secret scanning** with Gitleaks (full git history)
- **Vulnerability scanning** with Trivy (critical severities fail the build, high produces warnings)
- **License analysis and SBOM generation** with ORT (OSS Review Toolkit)

The license analysis applies our central license classifications and
evaluator rules, which categorize licenses as permissive, copyleft,
forbidden, etc. Forbidden licenses fail the build; copyleft licenses
produce warnings for committee review.

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

That is the entire integration step. The workflow handles installation,
configuration retrieval, and all checks.

For more detail, see [docs/onboarding.md](docs/onboarding.md).

## Repository structure

```
.github/workflows/full-check.yml   # the reusable workflow
policies/
  license-classifications.yml      # license categorization
  evaluator.rules.kts              # ORT evaluator rules in Kotlin DSL
docs/onboarding.md                 # integration guide
```

## Versioning

Stable, breaking-change-free integration is provided through the `v1`
git tag. Major-version increments may introduce breaking changes; minor
and patch updates are non-breaking.

## Governance

This repository is maintained by the Schwarz Digits Open Source
Committee. Changes to the workflow logic, the license classifications,
and the evaluator rules require committee review.

For questions, contact
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).

## License

Apache License 2.0. See [LICENSE](LICENSE).