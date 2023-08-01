@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.codeInsight.generation

import com.intellij.openapi.util.*

data class GenerateLocalisationsInFileContext(
    val fileName: String,
    val contextList: MutableList<GenerateLocalisationsContext>
) {
    companion object {
        val key = Key.create<GenerateLocalisationsInFileContext>("paradox.generateLocalisationInFileContext")
    }
}
