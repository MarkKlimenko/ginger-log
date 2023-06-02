package com.markklim.libs.ginger.masking

import com.markklim.libs.ginger.properties.LoggingProperties

class ParametersMasker {

    fun maskParameters(
        params: Map<String, String>,
        rules: List<LoggingProperties.LoggedEntitySettings.MaskedPropertyEntity>
    ): Map<String, String> {
        if (rules.isEmpty() || params.isEmpty()) {
            return params
        }

        val mutableParams: MutableMap<String, String> = params.toMutableMap()

        rules.forEach { rule ->
            maskForRule(mutableParams, rule)
        }

        return mutableParams
    }

    private fun maskForRule(
        mutableParams: MutableMap<String, String>,
        rule: LoggingProperties.LoggedEntitySettings.MaskedPropertyEntity
    ) {
        val requiredParam: String? = mutableParams[rule.property]

        if (requiredParam != null) {
            if (rule.valuePattern != null) {
                if (rule.valuePattern.toRegex().matches(requiredParam)) {
                    mutableParams[rule.property] = rule.substitutionValue
                }
            } else {
                mutableParams[rule.property] = rule.substitutionValue
            }
        }
    }
}