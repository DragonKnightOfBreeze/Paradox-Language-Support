@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.codeInsight.generation

import com.intellij.openapi.util.*
import icu.windea.pls.*

data class GenerateLocalisationsInFileContext(
    val fileName: String,
    val contextList: MutableList<GenerateLocalisationsContext>
)

val PlsKeys.generateLocalisationsInFileContext by lazy { Key.create<GenerateLocalisationsInFileContext>("paradox.generateLocalisationInFileContext") }