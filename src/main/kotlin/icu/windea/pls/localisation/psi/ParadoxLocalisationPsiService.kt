package icu.windea.pls.localisation.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import icu.windea.pls.core.isExactLineBreak

object ParadoxLocalisationPsiService {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is ParadoxLocalisationProperty
    }

    fun isIdElement(element: PsiElement?): Boolean {
        if (element == null) return false
        if (element.nextSibling.elementType in ParadoxLocalisationTokenSets.EXTRA_TEMPLATE_TOKENS) return false
        if (element.prevSibling.elementType in ParadoxLocalisationTokenSets.EXTRA_TEMPLATE_TOKENS) return false
        return true
    }

    fun isStrictPropertyContext(element: PsiElement) : Boolean {
        return element is ParadoxLocalisationFile || element is ParadoxLocalisationPropertyList
    }

    fun isStrictRichTextContext(element: PsiElement): Boolean {
        return element is ParadoxLocalisationFile || element.elementType in ParadoxLocalisationTokenSets.RICH_TEXT_CONTEXT_TOKENS
    }

    /**
     * 判断当前位置应当是一个语言区域（[ParadoxLocalisationLocale]），还是一个属性键（[ParadoxLocalisationPropertyKey]）。
     */
    fun isLocalisationLocaleLike(element: PsiElement): Boolean {
        val elementType = element.elementType
        when {
            elementType == ParadoxLocalisationElementTypes.PROPERTY_KEY_TOKEN -> {
                // 后面只能是空白或冒号,接下来的后面只能是空白，接着前面只能是空白，并且要在一行的开头
                val prevElement = element.prevLeaf(false)
                if (prevElement != null) {
                    val prevElementType = prevElement.elementType
                    if (prevElementType != TokenType.WHITE_SPACE || !prevElement.text.last().isExactLineBreak()) return false
                }
                val nextElement = element.nextSibling
                if (nextElement != null) {
                    val nextElementType = nextElement.elementType
                    if (nextElementType != ParadoxLocalisationElementTypes.COLON && nextElementType != TokenType.WHITE_SPACE) return false
                    val nextNextElement = nextElement.nextSibling
                    if (nextNextElement != null) {
                        val nextNextElementType = nextElement.elementType
                        if (nextNextElementType != TokenType.WHITE_SPACE) return false
                    }
                }
                return true
            }
            elementType == ParadoxLocalisationElementTypes.LOCALE_TOKEN -> {
                return true
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }
}
