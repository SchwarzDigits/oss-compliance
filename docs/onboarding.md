# Onboarding

How to integrate the central compliance workflow into your repository.

## Prerequisites

- The repository lives in one of our open-source organizations
  (`SchwarzDigits`, `SchwarzIT`).
- The repository has a default branch (typically `main`).
- The repository can run GitHub Actions (no special setup needed for
  public repositories).

## Step 1: Add the workflow file

Create `.github/workflows/compliance.yml` in your repository with the
following content:

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

That single line under `jobs:` references our central reusable
workflow. There is nothing else to configure for the default case.

## Step 2: Verify the workflow runs

Open a pull request (or trigger the workflow manually from the Actions
tab) and confirm that the `Compliance` job runs successfully.

The workflow consists of two parallel jobs:

1. **Secret and vulnerability scan** — runs Gitleaks and Trivy. Fast (1-2 minutes).
2. **License analysis and SBOM** — runs ORT. Slower (5-10 minutes for typical repositories).

If a job fails on the first run, read the section
["What the checks do"](#what-the-checks-do) below to understand which
check tripped and what is expected of you.

## Step 3: Make the workflow required (optional)

To prevent unchecked code from reaching `main`, configure the
relevant status checks as required on your default branch protection
rule. This is only available for public repositories on GitHub Free.

## What the checks do

### Secret scan (Gitleaks)

Scans the full git history for hardcoded credentials, API tokens, and
private keys. **Fails the build on any finding.**

If a secret is detected:

1. Do not just remove it from the latest commit. The secret is still
   in the git history and must be considered compromised.
2. Rotate the secret immediately.
3. Coordinate with the OSS Committee on history rewriting if needed.

### Vulnerability scan (Trivy)

Trivy checks dependencies against known CVE databases.

- **Critical severities fail the build.** Critical CVEs must be fixed
  before merge.
- **High severities produce warnings only.** They appear in the workflow
  output for awareness; the build does not fail.
- Lower severities are not currently flagged.

If a critical CVE is detected:

1. Update the affected dependency to a fixed version.
2. If no fix is available, evaluate whether the vulnerability is
   reachable in your code path. Document the finding and contact the
   OSS Committee for guidance.

### License analysis (ORT)

ORT (OSS Review Toolkit) analyzes all dependency manifests in your
repository, retrieves license information from the relevant package
registries, and applies our central license classifications.

The evaluator categorizes findings as:

- **Permissive** (Apache, MIT, BSD): always allowed, no action needed.
- **Copyleft-limited** (LGPL, MPL): allowed with awareness.
- **Copyleft (strong)** (GPL, AGPL): produces a **warning**. Confirm
  with the OSS Committee that the license is acceptable for your
  repository's outbound distribution.
- **Forbidden** (SSPL, BUSL, Commons-Clause): **fails the build**.
  Replace the dependency or contact the OSS Committee for an exception.
- **Unmapped**: produces an informational hint. Notify the OSS
  Committee so the central license classifications can be extended.

The full license classifications are defined in
[`policies/license-classifications.yml`](../policies/license-classifications.yml).
The evaluator rules are in
[`policies/evaluator.rules.kts`](../policies/evaluator.rules.kts).

### SBOM generation

ORT generates a Software Bill of Materials in SPDX and CycloneDX
formats as part of its reporter step. The SBOM contains all detected
dependencies with their license information and is uploaded as a
workflow artifact.

## Customization

The compliance workflow is intentionally not configurable. Tool
versions, severity thresholds, license classifications, and evaluator
rules are governed centrally by the OSS Committee, so that compliance
standards are applied consistently across all repositories.

If you have a legitimate need for an exception (e.g., a temporary
suppression of a finding while a fix is being released, or a
project-specific path exclusion), contact the OSS Committee. We can
discuss it case by case and update the central policy if appropriate.

## Known limitations

- The workflow does not currently include a CLA check. This will be
  added once the Schwarz Digits CLA is finalized.
- The license analysis runs on Linux runners. Repositories that need
  to build on macOS or Windows can run the central workflow alongside
  their own platform-specific CI.
- The following directories are excluded from secret and vulnerability
  scans, since they typically contain test fixtures, benchmarks,
  examples, or third-party vendored content that is not part of the
  production build: `node_modules`, `testdata`, `fixtures`,
  `_benchmark`, `_examples`, `_test`. ORT applies its own scoping
  rules based on package manager conventions.
- The license analysis takes 5-10 minutes, considerably longer than
  the secret and vulnerability scan. The two jobs run in parallel, so
  the overall workflow runtime is dominated by ORT.

## Getting help

For questions about the workflow or the policies it enforces, contact
the Schwarz Digits Open Source Committee at
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).