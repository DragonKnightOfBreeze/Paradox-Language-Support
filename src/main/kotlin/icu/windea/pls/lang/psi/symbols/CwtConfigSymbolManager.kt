@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.psi.symbols

import com.intellij.model.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.indexInfo.*

object CwtConfigSymbolManager {
    fun getSymbolInfo(element: CwtStringExpressionElement): CwtConfigSymbolIndexInfo? {
        if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return null

        val configType = getSymbolConfigType(element) ?: return null
        val gameType = getGameType(element) ?: return null
        val expressionText = element.value
        val name = getSymbolName(expressionText, configType) ?: return null
        val offset = expressionText.indexOf(name)
        if (offset == -1) return null
        return CwtConfigSymbolIndexInfo(name, configType.id, element.startOffset, gameType)
    }

    fun getSymbolDeclarations(element: CwtStringExpressionElement, offsetInElement: Int): Collection<PsiSymbolDeclaration> {
        if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return emptySet()

        val configType = getSymbolConfigType(element) ?: return emptySet()
        val gameType = getGameType(element) ?: return emptySet()
        val expressionText = element.value
        val name = getSymbolName(expressionText, configType) ?: return emptySet()
        val offset = expressionText.indexOf(name)
        if (offset == -1) return emptySet()
        val rangeInElement = TextRange.from(offset, name.length)
        //if(offsetInElement !in rangeInElement) return emptySet()
        if (offsetInElement < rangeInElement.startOffset || offsetInElement > rangeInElement.endOffset) return emptySet() //兼容等于 endOffset 的情况
        val symbol = CwtConfigSymbol(element, rangeInElement, name, configType, gameType)
        val declaration = CwtConfigSymbolDeclaration(element, symbol)
        return declaration.singleton().set()
    }

    fun getSymbolReferences(element: CwtStringExpressionElement): Collection<CwtConfigSymbolReference> {
        //TODO 2.0.1-dev 实际上可以引用于很多地方，如果需要精确实现，需要考虑进一步完善对规则文件的 schema 的支持

        if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return emptySet()

        val configType = CwtConfigManager.getConfigType(element)
        if (configType != null) return emptySet()

        val gameType = getGameType(element) ?: return emptySet()
        val expressionText = element.value

        //尝试从 typeExpression 中获取
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.definition
            val text = expressionText.removeSurroundingOrNull(prefix, suffix) ?: return@run
            val expression = ParadoxDefinitionTypeExpression.resolve(text)
            val keywords = mutableSetOf<String>()
            keywords += expression.type
            keywords += expression.subtypes
            val tuples = text.findKeywordsWithRanges(keywords)
            return tuples.map { (rangeInElement, keyword) ->
                val configType = if (keyword == expression.type) CwtConfigTypes.Type else CwtConfigTypes.Subtype
                CwtConfigSymbolReference(element, rangeInElement.shiftRight(prefix.length), keyword, configType, gameType)
            }
        }

        //尝试从 subtypeExpression 中获取
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.subtype
            val text = expressionText.removeSurroundingOrNull(prefix, suffix) ?: return@run
            val expression = ParadoxDefinitionSubtypeExpression.resolve(text)
            val keywords = expression.subtypes.map { it.value }.toSet()
            val tuples = text.findKeywordsWithRanges(keywords)
            return tuples.map { (rangeInElement, keyword) ->
                val configType = CwtConfigTypes.Subtype
                CwtConfigSymbolReference(element, rangeInElement.shiftRight(prefix.length), keyword, configType, gameType)
            }
        }

        //尝试从 dataExpression 中获取
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.enum
            val name = expressionText.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@run
            val rangeInElement = TextRange.from(prefix.length, name.length)
            val reference = CwtConfigSymbolReference(element, rangeInElement, name, CwtConfigTypes.Enum, gameType)
            return reference.singleton().set()
        }
        run {
            val patternSet = CwtConfigTextPatternSets.dynamicValueReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionText.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val rangeInElement = TextRange.from(prefix.length, name.length)
                val reference = CwtConfigSymbolReference(element, rangeInElement, name, CwtConfigTypes.DynamicValue, gameType)
                return reference.singleton().set()
            }
        }
        run {
            val patternSet = CwtConfigTextPatternSets.singleAliasReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionText.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val rangeInElement = TextRange.from(prefix.length, name.length)
                val reference = CwtConfigSymbolReference(element, rangeInElement, name, CwtConfigTypes.SingleAlias, gameType)
                return reference.singleton().set()
            }
        }
        run {
            val patternSet = CwtConfigTextPatternSets.aliasReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionText.removeSurroundingOrNull(prefix, suffix)?.substringBefore(':')?.orNull() ?: return@f
                val rangeInElement = TextRange.from(prefix.length, name.length)
                val reference = CwtConfigSymbolReference(element, rangeInElement, name, CwtConfigTypes.Alias, gameType)
                return reference.singleton().set()
            }
        }

        return emptySet()
    }

    fun resolveSymbolReference(reference: CwtConfigSymbolReference): Collection<Symbol> {
        val name = reference.name
        val type = reference.configType.id
        val configType = CwtConfigType.entries[type] ?: return emptySet()
        val gameType = reference.gameType
        val project = reference.element.project
        val result = mutableListOf<Symbol>()
        CwtConfigSymbolSearch.search(name, type, gameType, project, GlobalSearchScope.allScope(project)).processQuery p@{ info ->
            val elementOffset = info.elementOffset
            val file = info.virtualFile?.toPsiFile(project) ?: return@p true
            val e = file.findElementAt(elementOffset) ?: return@p true
            val element = e.parentOfType<CwtStringExpressionElement>() ?: return@p true
            if (element is CwtValue && !(element.isPropertyValue() || element.isBlockValue())) return@p true
            val expressionText = element.value
            val offset = expressionText.indexOf(name)
            if (offset == -1) return@p true
            val rangeInElement = TextRange.from(offset, name.length)
            val symbol = CwtConfigSymbol(element, rangeInElement, name, configType, gameType)
            result.add(symbol)
            true
        }
        return result
    }

    private fun getGameType(element: CwtStringExpressionElement): ParadoxGameType? {
        return CwtConfigManager.getContainingConfigGroup(element, forRepo = true)?.gameType
    }

    fun getSymbolConfigType(element: CwtStringExpressionElement): CwtConfigType? {
        val configType = CwtConfigManager.getConfigType(element)
        return when (configType) {
            CwtConfigTypes.Type, CwtConfigTypes.Subtype -> configType
            CwtConfigTypes.Enum, CwtConfigTypes.ComplexEnum -> CwtConfigTypes.Enum
            CwtConfigTypes.DynamicValueType -> configType
            CwtConfigTypes.SingleAlias -> configType
            CwtConfigTypes.Alias, CwtConfigTypes.Trigger, CwtConfigTypes.Effect, CwtConfigTypes.Modifier -> CwtConfigTypes.Alias
            else -> null
        }
    }

    fun getSymbolName(text: String, type: CwtConfigType): String? {
        return when (type) {
            CwtConfigTypes.Alias -> text.removeSurroundingOrNull("alias[", "]")?.substringBefore(":", "")?.orNull()
            else -> CwtConfigManager.getNameByConfigType(text, type)
        }
    }
}
