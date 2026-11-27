package icu.windea.pls.extensions.markdown

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxNameValidators
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

class MarkdownInlineCodeManager {
    fun getIdentifierText(element: PsiElement): String? {
        if (element.elementType != MarkdownElementTypes.CODE_SPAN) return null
        val idElement = element.firstChild?.nextSibling ?: return null
        if (idElement.prevSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        if (idElement.nextSibling?.elementType != MarkdownTokenTypes.BACKTICK) return null
        // NOTE 3.1.0 accept any leaf token (e.g., `TEXT`, GFM `AT_PATH_TOKEN`), not just plain text.
        return idElement.text
    }

    /**
     * 从 Markdown 内联代码 PSI 元素中提取目标名字信息。
     */
    fun resolveInfo(element: PsiElement): MarkdownInlineCodeInfo? {
        val identifier = getIdentifierText(element) ?: return null
        val prefix = if (identifier.startsWith('@')) "@" else ""
        val name = if (prefix.isEmpty()) identifier else identifier.removePrefixOrNull(prefix) ?: return null
        if (name.isEmpty() || name.containsBlank()) return null

        val openingLength = element.firstChild?.textLength ?: return null
        val closingLength = element.lastChild?.textLength ?: return null
        val startOffset = openingLength + prefix.length
        val endOffset = element.textLength - closingLength
        if (startOffset > endOffset) return null

        return MarkdownInlineCodeInfo(prefix, name, TextRange(startOffset, endOffset))
    }

    /**
     * 将内联代码中的目标名字解析为对应的 PSI 元素。
     *
     * @param element Markdown 内联代码 PSI 元素，用于提供解析上下文。
     */
    fun resolveTarget(element: PsiElement, prefix: String, name: String): PsiElement? {
        return when {
            prefix == "@" -> {
                if (!ParadoxNameValidators.checkScriptedVariableName(name)) return null
                val selector = ParadoxScriptedVariableSearch.selector(element.project, element).contextSensitive()
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).find()
            }
            prefix.isEmpty() -> {
                run {
                    val selector = ParadoxDefinitionSearch.selector(element.project, element).contextSensitive()
                    val result = ParadoxDefinitionSearch.searchElement(name, null, selector).find()
                    if (result != null) return result
                }
                if (!ParadoxNameValidators.checkLocalisationName(name)) return null
                val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                ParadoxLocalisationSearch.searchNormal(name, selector).find()
            }
            else -> null
        }
    }
}
