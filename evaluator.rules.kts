/*
 * Schwarz Digits Open Source — ORT Evaluator Rules
 *
 * Defines policy rules ORT applies to scanned projects, using the
 * license categorizations from license-classifications.yml.
 *
 * Severity levels:
 *   - error()   : fails the build (exit code 2 with fail-on=violations)
 *   - warning() : reported but does not fail the build
 *   - hint()    : informational
 *
 * Changes to this file require review by the OSS Committee.
 */

// Resolve license categories from license-classifications.yml.
val forbiddenLicenses = licenseClassifications.licensesByCategory["forbidden"].orEmpty()
val copyleftLicenses = licenseClassifications.licensesByCategory["copyleft"].orEmpty()
val copyleftLimitedLicenses = licenseClassifications.licensesByCategory["copyleft-limited"].orEmpty()
val permissiveLicenses = licenseClassifications.licensesByCategory["permissive"].orEmpty()
val publicDomainLicenses = licenseClassifications.licensesByCategory["public-domain"].orEmpty()
val documentationLicenses = licenseClassifications.licensesByCategory["documentation"].orEmpty()

// Set of all classified licenses (used to detect unmapped ones).
val classifiedLicenses = (
    forbiddenLicenses +
    copyleftLicenses +
    copyleftLimitedLicenses +
    permissiveLicenses +
    publicDomainLicenses +
    documentationLicenses
).toSet()

/**
 * Matcher: license is on the Schwarz Digits forbidden list.
 */
fun PackageRule.LicenseRule.isForbidden() = object : RuleMatcher {
    override val description = "isForbidden($license)"
    override fun matches() = license in forbiddenLicenses
}

/**
 * Matcher: license is a strong copyleft license.
 */
fun PackageRule.LicenseRule.isCopyleft() = object : RuleMatcher {
    override val description = "isCopyleft($license)"
    override fun matches() = license in copyleftLicenses
}

/**
 * Matcher: license is not classified in license-classifications.yml.
 */
fun PackageRule.LicenseRule.isUnclassified() = object : RuleMatcher {
    override val description = "isUnclassified($license)"
    override fun matches() = license !in classifiedLicenses
}

/**
 * Forbidden licenses fail the build.
 */
fun RuleSet.forbiddenLicenseRule() = packageRule("FORBIDDEN_LICENSE_IN_DEPENDENCY") {
    require {
        -isExcluded()
    }

    licenseRule("FORBIDDEN_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            +isForbidden()
        }

        error(
            "The dependency '${pkg.metadata.id.toCoordinates()}' uses license '$license', " +
                "which is on the Schwarz Digits forbidden list. " +
                "Replace this dependency or contact the OSS Committee for an exception.",
            "Replace the dependency with one that uses an acceptable license, or contact opensource@digits.schwarz."
        )
    }
}

/**
 * Strong copyleft licenses produce a warning for committee review.
 */
fun RuleSet.copyleftLicenseRule() = packageRule("COPYLEFT_LICENSE_IN_DEPENDENCY") {
    require {
        -isExcluded()
    }

    licenseRule("COPYLEFT_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            +isCopyleft()
        }

        warning(
            "The dependency '${pkg.metadata.id.toCoordinates()}' uses license '$license', " +
                "a strong copyleft license. Confirm that this is acceptable for your " +
                "repository's outbound license.",
            "If uncertain, contact opensource@digits.schwarz."
        )
    }
}

/**
 * Unclassified licenses produce a hint so we can extend the classification list.
 */
fun RuleSet.unclassifiedLicenseRule() = packageRule("UNCLASSIFIED_LICENSE") {
    require {
        -isExcluded()
    }

    licenseRule("UNCLASSIFIED_LICENSE", LicenseView.CONCLUDED_OR_DECLARED_OR_DETECTED) {
        require {
            +isUnclassified()
        }

        hint(
            "The dependency '${pkg.metadata.id.toCoordinates()}' uses license '$license', " +
                "which is not classified in license-classifications.yml. " +
                "Notify the OSS Committee so the classification can be extended.",
            "Email opensource@digits.schwarz with the package and license name."
        )
    }
}

// Apply the rules.
val ruleSet = ruleSet(ortResult, licenseInfoResolver, resolutionProvider) {
    forbiddenLicenseRule()
    copyleftLicenseRule()
    unclassifiedLicenseRule()
}

ruleViolations += ruleSet.violations