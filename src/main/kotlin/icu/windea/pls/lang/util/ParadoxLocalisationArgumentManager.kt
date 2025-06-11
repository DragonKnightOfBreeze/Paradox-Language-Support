package icu.windea.pls.lang.util

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.references.*

object ParadoxLocalisationArgumentManager {
    fun getReferences(element: ParadoxLocalisationArgument): Array<out PsiReference> {
        return CachedValuesManager.getCachedValue(element) {
            val value = doGetReferences(element)
            CachedValueProvider.Result.create(value, element)
        }
    }

    private fun doGetReferences(element: ParadoxLocalisationArgument): Array<out PsiReference> {
        val references = mutableListOf<PsiReference>()
        val argumentText = element.text
        run {
            if (element !is ParadoxLocalisationTextColorAwareElement) return@run
            val i = argumentText.indexOfFirst { ParadoxTextColorManager.isIdInArgument(it) }
            if (i == -1) return@run
            val rangeInElement = TextRange(i, i + 1)
            val reference = ParadoxLocalisationTextColorPsiReference(element, rangeInElement)
            references += reference
        }
        if (references.isEmpty()) return PsiReference.EMPTY_ARRAY
        return references.toTypedArray()
    }

    fun getInfo(element: ParadoxLocalisationArgument): String? {
        return getFormattingTagInfos(element.text).joinToString("<br>")
    }

    fun getFormattingTagInfos(text: String): Set<String> {
        // see:
        // https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/137

        if (text.isEmpty()) return emptySet()
        val set = mutableSetOf<String>()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '*' || c == '^' -> {
                    set += PlsDocBundle.message("formattingTag.1")
                }
                c == '=' -> {
                    set += PlsDocBundle.message("formattingTag.2")
                }
                c.isExactDigit() -> {
                    set += PlsDocBundle.message("formattingTag.3")
                }
                c == '%' -> {
                    if (text.getOrNull(i + 1) != '%') {
                        set += PlsDocBundle.message("formattingTag.4")
                    } else {
                        set += PlsDocBundle.message("formattingTag.5")
                    }
                }
                c == '+' -> {
                    set += PlsDocBundle.message("formattingTag.6")
                }
                c == '-' -> {
                    set += PlsDocBundle.message("formattingTag.7")
                }
            }
            i++
        }
        return set
    }
}
