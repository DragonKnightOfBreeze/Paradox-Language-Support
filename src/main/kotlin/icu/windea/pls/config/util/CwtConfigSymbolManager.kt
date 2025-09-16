@file:Suppress("UnstableApiUsage")

package icu.windea.pls.config.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.findKeywordsWithRanges
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.cwt.psi.isExpression
import icu.windea.pls.lang.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.references.cwt.CwtConfigSymbolPsiReference
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.CwtConfigTextPatternSets
import icu.windea.pls.model.constants.CwtConfigTextPatterns
import icu.windea.pls.model.indexInfo.CwtConfigSymbolIndexInfo

object CwtConfigSymbolManager {
    object Keys : KeyRegistry() {
        val cachedSymbolInfos by createKey<CachedValue<List<CwtConfigSymbolIndexInfo>>>(Keys)
    }

    // NOTE 相比 Symbol API，通过实现继承自 CwtMockPsiElement 的 CwtConfigSymbolElement ，应当能更加简单地实现相关功能（且区分读写访问）

    fun getInfos(element: CwtStringExpressionElement): List<CwtConfigSymbolIndexInfo> {
        if (!element.isExpression()) return emptyList()
        ProgressManager.checkCanceled()
        val infos = doGetInfoFromCache(element)
        return infos
    }

    fun getReferences(element: CwtStringExpressionElement): Array<out PsiReference> {
        if (!element.isExpression()) return PsiReference.EMPTY_ARRAY
        ProgressManager.checkCanceled()
        val infos = doGetInfoFromCache(element)
        if (infos.isEmpty()) return PsiReference.EMPTY_ARRAY
        return infos.map { CwtConfigSymbolPsiReference(element, TextRange.from(it.offset, it.name.length), it) }.toTypedArray()
    }

    private fun doGetInfoFromCache(element: CwtStringExpressionElement): List<CwtConfigSymbolIndexInfo> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedSymbolInfos) {
            val value = doGetInfos(element)
            value.withDependencyItems(element, PsiModificationTracker.getInstance(element.project).forLanguage(CwtLanguage))
        }
    }

    private fun doGetInfos(element: CwtStringExpressionElement): List<CwtConfigSymbolIndexInfo> {
        val infos = mutableListOf<CwtConfigSymbolIndexInfo>()
        collectInfos(element, infos)
        return infos.optimized()
    }

    private fun collectInfos(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>) {
        val gameType = getGameType(element) ?: return
        val expressionString = element.value
        val quoteOffset = if (element.text.isLeftQuoted()) 1 else 0
        collectInfosFromDeclarations(element, infos, gameType, expressionString, quoteOffset)
        collectInfosFromReferences(element, infos, gameType, expressionString, quoteOffset)
    }

    private fun collectInfosFromDeclarations(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val configType = CwtConfigManager.getConfigType(element) ?: return
        val symbolConfigType = getSymbolConfigType(configType) ?: return
        val name = getSymbolName(expressionString, configType) ?: return
        val nameOffset = expressionString.indexOf(name)
        if (nameOffset == -1) return
        val tuples = buildList b@{
            if (symbolConfigType != CwtConfigTypes.Alias) {
                add(tupleOf(name, nameOffset, symbolConfigType))
                return@b
            }

            // aliases
            val n1 = name.substringBefore(':').orNull() ?: return@b
            add(tupleOf(n1, nameOffset, symbolConfigType))
            // effects & triggers
            if (configType != CwtConfigTypes.Trigger && configType != CwtConfigTypes.Effect) return@b
            val n2 = name.substringAfter(':').orNull() ?: return@b
            if (CwtDataExpression.resolve(n2, false).type != CwtDataTypes.Constant) return@b
            add(tupleOf(n2, expressionString.indexOf(':') + 1, configType))
        }
        tuples.forEach f@{ (symbolName, symbolOffset, symbolConfigType) ->
            val readWriteAccess = ReadWriteAccessDetector.Access.Write
            val nextOffset = offset + symbolOffset
            val info = CwtConfigSymbolIndexInfo(symbolName, symbolConfigType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
    }

    private fun collectInfosFromReferences(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // TODO 2.0.1-dev+ 实际上可以引用于很多地方，如果需要精确实现，需要考虑进一步完善对规则文件的 schema 的支持

        val configType = CwtConfigManager.getConfigType(element)
        run {
            if (configType != null) return@run
            collectInfosFromSubtypeExpressions(element, infos, gameType, expressionString, offset)
            collectInfosFromTypeExpressions(element, infos, gameType, expressionString, offset)
            collectInfosFromCommonDataExpressions(element, infos, gameType, expressionString, offset)
            collectInfosFromAliasDataExpressions(element, infos, gameType, expressionString, offset)
        }
        run {
            if (configType == null) return@run
            val symbolConfigType = getSymbolConfigType(configType) ?: return@run
            if (symbolConfigType != CwtConfigTypes.Alias) return@run
            val (prefix, suffix, separator) = CwtConfigTextPatterns.alias
            val s = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@run
            val separatorIndex = s.indexOf(separator)
            if (separatorIndex == -1) return@run
            val e = s.substring(separatorIndex + 1)
            val nextOffset = offset + prefix.length + separatorIndex + 1
            collectInfosFromTypeExpressions(element, infos, gameType, e, nextOffset)
            collectInfosFromCommonDataExpressions(element, infos, gameType, e, nextOffset)
        }
    }

    private fun collectInfosFromSubtypeExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // 尝试从 typeExpression 中获取
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val (prefix, suffix) = CwtConfigTextPatterns.definition
        val text = expressionString.removeSurroundingOrNull(prefix, suffix) ?: return
        val expression = ParadoxDefinitionTypeExpression.resolve(text)
        val keywords = mutableSetOf<String>()
        keywords += expression.type
        keywords += expression.subtypes
        val tuples = text.findKeywordsWithRanges(keywords)
        if (tuples.isEmpty()) return
        tuples.mapTo(infos) { (rangeInElement, keyword) ->
            val configType = if (keyword == expression.type) CwtConfigTypes.Type else CwtConfigTypes.Subtype
            val nextOffset = offset + prefix.length + rangeInElement.startOffset
            CwtConfigSymbolIndexInfo(keyword, configType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
        }
    }

    private fun collectInfosFromTypeExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // 尝试从 typeExpression 中获取
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val (prefix, suffix) = CwtConfigTextPatterns.definition
        val text = expressionString.removeSurroundingOrNull(prefix, suffix) ?: return
        val expression = ParadoxDefinitionTypeExpression.resolve(text)
        val keywords = mutableSetOf<String>()
        keywords += expression.type
        keywords += expression.subtypes
        val tuples = text.findKeywordsWithRanges(keywords)
        if (tuples.isEmpty()) return
        tuples.mapTo(infos) { (rangeInElement, keyword) ->
            val configType = if (keyword == expression.type) CwtConfigTypes.Type else CwtConfigTypes.Subtype
            val nextOffset = offset + prefix.length + rangeInElement.startOffset
            CwtConfigSymbolIndexInfo(keyword, configType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
        }
    }

    private fun collectInfosFromCommonDataExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.enum
            val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@run
            val nextOffset = offset + prefix.length
            val info = CwtConfigSymbolIndexInfo(name, CwtConfigTypes.Enum.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
        run {
            val patternSet = CwtConfigTextPatternSets.dynamicValueReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val nextOffset = offset + prefix.length
                val info = CwtConfigSymbolIndexInfo(name, CwtConfigTypes.DynamicValue.id, readWriteAccess, nextOffset, element.startOffset, gameType)
                infos += info
            }
        }
        run {
            val patternSet = CwtConfigTextPatternSets.singleAliasReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val nextOffset = offset + prefix.length
                val info = CwtConfigSymbolIndexInfo(name, CwtConfigTypes.SingleAlias.id, readWriteAccess, nextOffset, element.startOffset, gameType)
                infos += info
            }
        }
    }

    private fun collectInfosFromAliasDataExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolIndexInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val readWriteAccess = ReadWriteAccessDetector.Access.Read
        val patternSet = CwtConfigTextPatternSets.aliasReference
        patternSet.forEach f@{ pattern ->
            val (prefix, suffix) = pattern
            val name = expressionString.removeSurroundingOrNull(prefix, suffix) ?: return@f
            val nextOffset = offset + prefix.length
            val info = CwtConfigSymbolIndexInfo(name, CwtConfigTypes.Alias.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
    }

    private fun getGameType(element: CwtStringExpressionElement): ParadoxGameType? {
        return CwtConfigManager.getContainingConfigGroup(element)?.gameType
    }

    private fun getSymbolConfigType(configType: CwtConfigType): CwtConfigType? {
        return when (configType) {
            CwtConfigTypes.Type, CwtConfigTypes.Subtype -> configType
            CwtConfigTypes.Enum, CwtConfigTypes.ComplexEnum -> CwtConfigTypes.Enum
            CwtConfigTypes.DynamicValueType -> configType
            CwtConfigTypes.SingleAlias -> configType
            CwtConfigTypes.Alias, CwtConfigTypes.Trigger, CwtConfigTypes.Effect, CwtConfigTypes.Modifier -> CwtConfigTypes.Alias
            else -> null
        }
    }

    private fun getSymbolName(text: String, configType: CwtConfigType): String? {
        return when (configType) {
            CwtConfigTypes.Alias, CwtConfigTypes.Trigger, CwtConfigTypes.Effect, CwtConfigTypes.Modifier -> text.removeSurroundingOrNull("alias[", "]")?.orNull()
            else -> CwtConfigManager.getNameByConfigType(text, configType)
        }
    }
}
