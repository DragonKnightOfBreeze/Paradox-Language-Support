@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.codeInsight.generation

import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

data class GenerateLocalisationsContext(
    val definitionName: String,
    val localisationNames: Set<String>
)

private val _generateLocalisationsContextKey = Key.create<GenerateLocalisationsContext>("paradox.generateLocalisationContext")

val PlsKeys.generateLocalisationsContextKey get() = _generateLocalisationsContextKey