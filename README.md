# oss-compliance

Central compliance tooling for the Schwarz Digits open-source program.

This repository serves two purposes:

1. **Reusable GitHub Actions workflow** for compliance checks in our
   open-source repositories.
2. **ORT configuration repository** containing the license
   classifications, evaluator rules, and path excludes that ORT
   applies during the license analysis.

## What it does

When integrated into a repository, the workflow runs on every pull request, every push to the default branch,
and once weekly on Sunday evenings, performing:

- **Secret scanning** with Gitleaks (full git history)
- **Vulnerability scanning** with Trivy (critical severities fail the build, high produces warnings)
- **License analysis and SBOM generation** with ORT (OSS Review Toolkit)

The license analysis applies our central license classifications and
evaluator rules. Forbidden licenses fail the build; strong copyleft
licenses produce warnings for committee review.

## Quick start

Add the following workflow to your repository at
`.github/workflows/compliance.yml`:

```yaml
name: Compliance

on:
   pull_request:
   push: { branches: [main] }
   schedule:
      - cron: '43 21 * * 0'
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
.ort.yml                           # path excludes (injected into all scans)
config.yml                         # ORT runtime configuration (scanners, etc.)
license-classifications.yml        # license categorization (used by evaluator)
evaluator.rules.kts                # ORT evaluator rules in Kotlin DSL
docs/onboarding.md                 # integration guide
```

The ORT-specific configuration files (`.ort.yml`, `config.yml`,
`license-classifications.yml`, `evaluator.rules.kts`) live in the
repository root, because that is where ORT expects them when it pulls
the configuration repository.

## Versioning

Stable, breaking-change-free integration is provided through the `v1`
git tag. Major-version increments may introduce breaking changes; minor
and patch updates are non-breaking.

## Governance

This repository is maintained by the Schwarz Digits Open Source
Committee. Changes to the workflow logic, the license classifications,
the evaluator rules, and the path excludes require committee review.

For questions, contact
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).

## License

Apache License 2.0. See [LICENSE](LICENSE).