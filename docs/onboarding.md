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
  schedule:
    - cron: '17 3 * * *'  # nightly at 03:17 UTC; pick a different minute per repo
  workflow_dispatch:

jobs:
  compliance:
    uses: SchwarzDigits/oss-compliance/.github/workflows/full-check.yml@v1
```

That single line under `jobs:` references our central reusable
workflow. There is nothing else to configure for the default case.

**Note on the `schedule` trigger:** GitHub recommends spreading
scheduled workflows across the hour to avoid load spikes. Pick a
unique minute (between 0 and 59) for your repository. The hour
should remain `3` (03:xx UTC) so all our compliance runs cluster in
the same time window for easier monitoring.

## Step 2: Verify the workflow runs

Open a pull request (or trigger the workflow manually from the Actions
tab) and confirm that the `Compliance` job runs successfully.

The workflow is split into three jobs that follow a hybrid execution model:

1. **Secret and vulnerability scan** — runs on every trigger (PR, push,
   schedule, manual). Fast (1-2 minutes).
2. **Decide if ORT runs** — a small job that checks whether the
   license analysis is needed for this run. It returns true on
   schedule/manual triggers, and on PR/push only when a dependency
   manifest changed (go.mod, package.json, pom.xml, etc.).
3. **License analysis and SBOM** — runs ORT, but only when the
   previous job decided it should. Slower (5-10 minutes).

**This means:** Most PRs that only change source code will skip the
license analysis entirely and complete in 1-2 minutes. PRs that
change dependencies will get the full check. The nightly run always
performs the full check, so we have a daily safety net regardless of
PR activity.

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
[`license-classifications.yml`](../license-classifications.yml). The
evaluator rules are in [`evaluator.rules.kts`](../evaluator.rules.kts).

### SBOM generation

ORT generates a Software Bill of Materials in SPDX and CycloneDX
formats as part of its reporter step. The SBOM contains all detected
dependencies with their license information and is uploaded as a
workflow artifact.

## Path excludes

The workflow excludes test fixtures, benchmarks, examples, and
vendored npm packages from analysis:

- `_benchmark/**`, `_examples/**`, `_test/**` (underscore-prefixed
  conventions for non-distributed code)
- `testdata/**`, `fixtures/**` (common test data conventions)
- `node_modules/**` (vendored npm packages)

These excludes are applied centrally via a `.ort.yml` that the
workflow injects into every repository before ORT runs. Repositories
do not need to maintain their own `.ort.yml`.

If your repository already contains a `.ort.yml`, the workflow will
detect this, skip the injection, and emit a warning. In this case,
the repository's local `.ort.yml` takes precedence. We do not
recommend doing this without consulting the OSS Committee, since the
goal of the central configuration is consistent compliance behavior
across all repositories.

## Customization

The compliance workflow is intentionally not configurable. Tool
versions, severity thresholds, license classifications, evaluator
rules, and path excludes are governed centrally by the OSS Committee,
so that compliance standards are applied consistently across all
repositories.

If you have a legitimate need for an exception (e.g., a temporary
suppression of a finding while a fix is being released, or a
project-specific path exclude), contact the OSS Committee. We can
discuss it case by case and update the central policy if appropriate.

## Known limitations

- The workflow does not currently include a CLA check. This will be
  added once the Schwarz Digits CLA is finalized.
- The license analysis runs on Linux runners. Repositories that need
  to build on macOS or Windows can run the central workflow alongside
  their own platform-specific CI.
- The license analysis takes 5-10 minutes when it runs. To keep PR
  feedback fast, ORT only runs when a dependency manifest changed in
  the PR or push, plus once nightly regardless. Most PRs see the
  workflow complete in 1-2 minutes (Trivy and Gitleaks only).
- The nightly schedule produces a daily SBOM, License-Report, and
  Compliance status as workflow artifacts. The OSS Committee monitors
  these centrally.

## Getting help

For questions about the workflow or the policies it enforces, contact
the Schwarz Digits Open Source Committee at
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).