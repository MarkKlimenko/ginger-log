package com.markklim.libs.ginger.extractor.utils

fun extractStringFromList(strings: Collection<String>): String {
    return if (strings.size > 1) {
        strings.joinToString(separator = ",")
    } else {
        strings.firstOrNull().orEmpty()
    }
}
