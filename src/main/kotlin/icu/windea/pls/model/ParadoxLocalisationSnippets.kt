package icu.windea.pls.model

import com.intellij.openapi.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationSnippets(
    val key: String, // KEY
    val prefix: String, // KEY:0
    val text: String, // TEXT
    val textRange: TextRange,
) {
    @Volatile
    var newText: String = text

    fun render(): String {
        return "$prefix \"$text\"" // KEY:0 "TEXT"
    }

    fun renderNew(): String {
        return "$prefix \"$newText\"" // KEY:0 "NEW TEXT"
    }

    companion object {
        @JvmStatic
        fun from(element: ParadoxLocalisationProperty): ParadoxLocalisationSnippets {
            val name = element.name
            val elementText = element.text
            val i1 = elementText.indexOf('"')
            if (i1 == -1) {
                val prefix = elementText.trimEnd()
                val textRange = TextRange.from(elementText.length, 0)
                return ParadoxLocalisationSnippets(name, prefix, "", textRange)
            }
            val prefix = elementText.substring(0, i1).trimEnd()
            val text = elementText.substring(i1 + 1)
                .let { if (it.lastOrNull() == '"') it.dropLast(1) else it }
            val textRange = TextRange.create(i1, elementText.length)
            return ParadoxLocalisationSnippets(name, prefix, text, textRange)
        }
    }
}
