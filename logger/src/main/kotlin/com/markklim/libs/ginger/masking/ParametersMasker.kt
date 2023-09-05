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

        val mutableParams: MutableMap<String, Pair<String, String>> = params.entries
            .associate { it.key.lowercase() to it.toPair() }
            .toMutableMap()

        rules.forEach { rule ->
            maskForRule(mutableParams, rule)
        }

        return mutableParams.entries
            .associate { it.value.first to it.value.second }
    }

    private fun maskForRule(
        mutableParams: MutableMap<String, Pair<String, String>>,
        rule: LoggingProperties.LoggedEntitySettings.MaskedPropertyEntity
    ) {
        val ruleProperty: String = rule.property.lowercase()
        val requiredParamPair: Pair<String, String>? = mutableParams[ruleProperty]

        if (requiredParamPair != null) {
            if (rule.valuePattern != null) {
                if (rule.valuePattern.toRegex(RegexOption.IGNORE_CASE).matches(requiredParamPair.second)) {
                    mutableParams[ruleProperty] = Pair(requiredParamPair.first, rule.substitutionValue)
                }
            } else {
                mutableParams[ruleProperty] = Pair(requiredParamPair.first, rule.substitutionValue)
            }
        }
    }
}