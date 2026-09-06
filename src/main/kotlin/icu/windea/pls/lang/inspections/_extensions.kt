package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.options.OptExpandableString
import icu.windea.pls.ChronicleBundle

fun OptExpandableString.forPatterns() = description(ChronicleBundle.message("comment.patterns"))

fun OptExpandableString.forAntPatterns() = description(ChronicleBundle.message("comment.antPatterns"))
