/*
 * Schwarz Digits Open Source — ORT Evaluator Rules
 *
 * This file defines the policy rules that ORT applies to scanned projects.
 * The rules use the license categorizations defined in license-classifications.yml.
 *
 * Severity levels:
 *   - ERROR   : fails the build
 *   - WARNING : reported but does not fail the build
 *   - HINT    : informational
 *
 * Changes to this file require review by the OSS Committee.
 */

// Forbidden licenses fail the build.
val forbiddenLicenseInDependencyRule = packageRule("FORBIDDEN_LICENSE_IN_DEPENDENCY") {
    require {
        -isProject()
        -isExcluded()
    }

    licenseRule("FORBIDDEN_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            +isInLicenseCategory("forbidden")
        }

        error(
            "The dependency ${pkg.metadata.id.toCoordinates()} uses license $license, " +
                "which is on the Schwarz Digits forbidden list. " +
                "Replace this dependency or contact the OSS Committee for an exception.",
            "Replace the dependency with one that uses an acceptable license."
        )
    }
}

// Strong copyleft licenses produce a warning so the committee can review.
val copyleftLicenseInDependencyRule = packageRule("COPYLEFT_LICENSE_IN_DEPENDENCY") {
    require {
        -isProject()
        -isExcluded()
    }

    licenseRule("COPYLEFT_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            +isInLicenseCategory("copyleft")
        }

        warning(
            "The dependency ${pkg.metadata.id.toCoordinates()} uses license $license, " +
                "a strong copyleft license. Please confirm that this is acceptable for " +
                "your repository's outbound license.",
            "Discuss with the OSS Committee if uncertain."
        )
    }
}

// Unmapped licenses are flagged as hints so we can extend the classification list.
val unmappedLicenseRule = packageRule("UNMAPPED_LICENSE") {
    require {
        -isProject()
        -isExcluded()
    }

    licenseRule("UNMAPPED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_AND_DETECTED) {
        require {
            -isInLicenseCategory("permissive")
            -isInLicenseCategory("copyleft-limited")
            -isInLicenseCategory("copyleft")
            -isInLicenseCategory("forbidden")
            -isInLicenseCategory("public-domain")
            -isInLicenseCategory("documentation")
        }

        hint(
            "The dependency ${pkg.metadata.id.toCoordinates()} uses license $license, " +
                "which is not classified in our license-classifications.yml. " +
                "Please notify the OSS Committee so the classification can be extended.",
            "Add the license to the central license classifications."
        )
    }
}

ruleViolations += listOf(
    forbiddenLicenseInDependencyRule,
    copyleftLicenseInDependencyRule,
    unmappedLicenseRule
).flatMap { it.violations }