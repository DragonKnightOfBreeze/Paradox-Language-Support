package icu.windea.pls.extension.markdown.psi

import com.intellij.model.*
import com.intellij.model.psi.*
import com.intellij.openapi.options.advanced.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.extension.markdown.*
import icu.windea.pls.lang.references.symbols.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.constants.*

/**
 * 用于支持在markdown文件中，将内联代码尝试没戏为匹配的目标引用（定义、本地化等）。
 */
@Suppress("UnstableApiUsage")
class MarkdownInlineCodeReferenceProvider : ImplicitReferenceProvider {
    override fun getImplicitReference(element: PsiElement, offsetInElement: Int): PsiSymbolReference? {
        if (!AdvancedSettings.getBoolean("pls.md.resolveInlineCodes")) return null
        val identifier = PlsMarkdownManager.getIdentifierFromInlineCode(element) ?: return null

        run {
            val name = identifier.removePrefixOrNull("@") ?: return@run
            if (name.isEmpty() || name.containsBlank()) return@run
            val textRange = TextRange(2, element.text.length - 1)
            return SymbolReference(element, textRange, "@", name)
        }
        run {
            val name = identifier
            if (name.isEmpty() || name.containsBlank()) return@run
            val textRange = TextRange(1, element.text.length - 1)
            return SymbolReference(element, textRange, "", name)
        }

        return null
    }

    class SymbolReference(
        element: PsiElement,
        rangeInElement: TextRange,
        val prefix: String,
        val name: String
    ) : ParadoxSymbolReferenceBase(element, rangeInElement) {
        override fun resolveReference(): Collection<Symbol> {
            //如果带有前缀 @ ，则尝试解析为脚本变量
            //否则，尝试解析为定义或者本地化

            when {
                prefix == "@" -> {
                    if (!PlsPatternConstants.scriptedVariableName.matches(name)) return emptySet()
                    val selector = selector(element.project, element).scriptedVariable().contextSensitive()
                    val result = ParadoxGlobalScriptedVariableSearch.search(name, selector).find() ?: return emptySet()
                    return result.asSymbol().singleton().set()
                }
                prefix.isEmpty() -> {
                    run {
                        val selector = selector(element.project, element).definition().contextSensitive()
                        val result = ParadoxDefinitionSearch.search(name, null, selector).find() ?: return@run
                        return result.asSymbol().singleton().set()
                    }
                    run {
                        if (!PlsPatternConstants.localisationName.matches(name)) return@run
                        val selector = selector(element.project, element).localisation().contextSensitive()
                            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                        val result = ParadoxLocalisationSearch.search(name, selector).find() ?: return@run
                        return result.asSymbol().singleton().set()
                    }
                    return emptySet()
                }
                else -> return emptySet()
            }
        }
    }
}

