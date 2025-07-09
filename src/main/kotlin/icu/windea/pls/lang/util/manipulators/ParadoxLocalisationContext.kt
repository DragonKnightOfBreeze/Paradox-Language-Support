package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

data class ParadoxLocalisationContext(
    val element: ParadoxLocalisationProperty,
    val key: String, // KEY
    val prefix: String, // KEY:0
    val text: String, // TEXT
    val textRange: TextRange,
    val shouldHandle: Boolean,
) {
    @Volatile
    var newText: String = text

    fun join(): String {
        return "$prefix \"$text\"" // KEY:0 "TEXT"
    }

    fun joinWithNewText(): String {
        return "$prefix \"$newText\"" // KEY:0 "NEW TEXT"
    }

    companion object {
        @JvmStatic
        fun from(element: ParadoxLocalisationProperty): ParadoxLocalisationContext {
            val name = element.name
            val elementText = element.text
            val i1 = elementText.indexOf('"')
            if (i1 == -1) {
                val prefix = elementText.trimEnd()
                val textRange = TextRange.from(elementText.length, 0)
                return ParadoxLocalisationContext(element, name, prefix, "", textRange, false)
            }
            val prefix = elementText.substring(0, i1).trimEnd()
            val text = elementText.substring(i1 + 1)
                .let { if (it.lastOrNull() == '"') it.dropLast(1) else it }
            val textRange = TextRange.create(i1, elementText.length)
            val shouldHandle = shouldHandle(element)
            return ParadoxLocalisationContext(element, name, prefix, text, textRange, shouldHandle)
        }

        private fun shouldHandle(element: ParadoxLocalisationProperty): Boolean {
            val pv = element.propertyValue ?: return false
            var r = false
            pv.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is ParadoxLocalisationString) {
                        visitString(element)
                        return
                    }
                    super.visitElement(element)
                }

                private fun visitString(element: ParadoxLocalisationString) {
                    val s = element.text
                    if (checkString(s)) {
                        r = true
                    }
                }

                private fun checkString(s: String): Boolean {
                    //存在任意非空白、非数字的字符
                    return s.any { !it.isWhitespace() || !it.isExactDigit() }
                }
            })
            return r
        }
    }
}
