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
tab) and confirm that the `OSS compliance checks` job runs successfully.

If the job fails on the first run, read the section
["What the checks do"](#what-the-checks-do) below to understand which
check tripped and what is expected of you.

## Step 3: Make the workflow required (optional)

To prevent unchecked code from reaching `main`, configure the
`OSS compliance checks` status check as a required check on your
default branch protection rule. This is only available for public
repositories on GitHub Free.

## What the checks do

### Secret scan (Gitleaks)

Scans the full git history for hardcoded credentials, API tokens, and
private keys. **Fails the build on any finding.**

If a secret is detected:

1. Do not just remove it from the latest commit. The secret is still
   in the git history and must be considered compromised.
2. Rotate the secret immediately.
3. Coordinate with the OSS Committee on history rewriting if needed.

### License compliance

Trivy scans all dependency manifests (`go.mod`, `package.json`, etc.)
and detects each dependency's license. Findings are compared against
the [central denylist](../policies/denylist.yaml).

**Fails the build if a forbidden license is detected.**

If a forbidden license is detected:

1. Determine whether the dependency is required.
2. If yes, find an alternative with a compatible license.
3. If no acceptable alternative exists, contact the OSS Committee for
   review. There may be exceptions, but they require explicit approval.

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

### SBOM generation

A Software Bill of Materials is generated in both SPDX and CycloneDX
formats and uploaded as a workflow artifact. SBOMs are retained for
90 days.

This step does not fail the build.

## Customization

The compliance workflow is intentionally not configurable. Tool
versions, severity thresholds, and the license denylist are governed
centrally by the OSS Committee, so that compliance standards are
applied consistently across all repositories.

If you have a legitimate need for an exception (e.g., a temporary
suppression of a finding while a fix is being released, or a
project-specific path exclusion), contact the OSS Committee. We can
discuss it case by case and update the central policy if appropriate.

## Known limitations

- The workflow does not currently include a CLA check. This will be
  added once the Schwarz Digits CLA is finalized.
- The workflow runs only on Linux runners. Repositories that need to
  build on macOS or Windows can run the central workflow alongside
  their own platform-specific CI.
- License detection is dependency-based, not source-file-based. License
  headers in your own source files are not enforced by this workflow.
- The following directories are excluded from all scans, since they
  typically contain test fixtures, benchmarks, examples, or third-party
  vendored content that is not part of the production build:
  `node_modules`, `testdata`, `fixtures`, `_benchmark`, `_examples`,
  `_test`. If your repository uses other names for these purposes,
  add a `.trivyignore` file at the repository root with paths to skip,
  and contact the OSS Committee if SBOM exclusions are needed beyond
  this default set.

## Getting help

For questions about the workflow or the policies it enforces, contact
the Schwarz Digits Open Source Committee at
[opensource@digits.schwarz](mailto:opensource@digits.schwarz).