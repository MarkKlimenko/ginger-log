package com.markklim.libs.ginger.masking

import com.markklim.libs.ginger.properties.LoggingProperties

class ParametersMasker {

    // TODO: create maskers
    //fun maskParameters(params: Map<String, Any>,
    //                          rules: List<LoggingProperties.MaskedEntity>): Map<String, Any> {
    //    return params.mapValues {
//
    //        // TODO: each by patterns except of values
    //        rules.firstOrNull { rule -> rule.displayedName.equals(it.key, true) }
    //        ?.let {rule ->  rule.sensitiveDataPattern
    //            ?.matcher(it.value.toString())
    //            ?.replaceAll(rule.substitutionValue)
    //            ?.toString()}
    //        ?: it.value
    //    }
    //}
}