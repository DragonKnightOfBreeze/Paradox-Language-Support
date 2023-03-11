@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.codeInsight.generation

import com.intellij.openapi.util.*
import icu.windea.pls.*

data class GenerateLocalisationsInFileContext(
    val fileName: String,
    val contextList: MutableList<GenerateLocalisationsContext>
)

private val _generateLocalisationsInFileContextKey = Key.create<GenerateLocalisationsInFileContext>("paradox.generateLocalisationInFileContext")

val PlsKeys.generateLocalisationsInFileContextKey get() = _generateLocalisationsInFileContextKey