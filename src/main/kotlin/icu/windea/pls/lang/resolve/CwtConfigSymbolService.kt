package icu.windea.pls.lang.resolve

import com.intellij.psi.util.startOffset
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.CwtConfigTypes
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.findKeywordsWithTextRanges
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.model.CwtConfigSymbolInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.CwtConfigTextPatternSets
import icu.windea.pls.model.constants.CwtConfigTextPatterns
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression

object CwtConfigSymbolService {
    fun resolveInfos(element: CwtStringExpressionElement): List<CwtConfigSymbolInfo> {
        val infos = mutableListOf<CwtConfigSymbolInfo>()
        collectInfos(element, infos)
        return infos
    }

    private fun collectInfos(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>) {
        val gameType = getGameType(element) ?: return
        val expressionString = element.value
        val quoteOffset = if (element.text.isLeftQuoted()) 1 else 0
        collectInfosFromDeclarations(element, infos, gameType, expressionString, quoteOffset)
        collectInfosFromReferences(element, infos, gameType, expressionString, quoteOffset)
    }

    private fun collectInfosFromDeclarations(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val configType = CwtConfigManager.getConfigType(element) ?: return
        val symbolConfigType = getSymbolConfigType(configType) ?: return
        val name = getSymbolName(expressionString, configType) ?: return
        val nameOffset = expressionString.indexOf(name)
        if (nameOffset == -1) return
        val tuples = buildList b@{
            if (symbolConfigType != CwtConfigTypes.Alias) {
                this += tupleOf(name, nameOffset, symbolConfigType)
                return@b
            }

            // aliases
            val n1 = name.substringBefore(':').orNull() ?: return@b
            this += tupleOf(n1, nameOffset, symbolConfigType)
            // modifiers & effects & triggers
            if (configType != CwtConfigTypes.Modifier && configType != CwtConfigTypes.Trigger && configType != CwtConfigTypes.Effect) return@b
            val n2 = name.substringAfter(':').orNull() ?: return@b
            if (CwtDataExpression.resolve(n2, CwtDataExpressionRole.Value).type != CwtDataTypes.Constant) return@b
            this += tupleOf(n2, expressionString.indexOf(':') + 1, configType)
        }
        tuples.forEachFast f@{ (symbolName, symbolOffset, symbolConfigType) ->
            val readWriteAccess = ReadWriteAccess.Write
            val nextOffset = offset + symbolOffset
            val info = CwtConfigSymbolInfo(symbolName, symbolConfigType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
    }

    private fun collectInfosFromReferences(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // TODO [config-system] 2.0.1-dev+ 实际上可以引用于很多地方，如果需要精确实现，需要考虑进一步完善对规则文件的 schema 的支持

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

    private fun collectInfosFromSubtypeExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // 尝试从 typeExpression 中获取
        val readWriteAccess = ReadWriteAccess.Read
        val (prefix, suffix) = CwtConfigTextPatterns.definition
        val text = expressionString.removeSurroundingOrNull(prefix, suffix) ?: return
        val expression = ParadoxDefinitionTypeExpression.resolve(text)
        val keywords = mutableSetOf<String>()
        keywords += expression.type
        keywords += expression.subtypes
        val tuples = text.findKeywordsWithTextRanges(keywords)
        if (tuples.isEmpty()) return
        tuples.forEachFast { (keyword, rangeInElement) ->
            val configType = if (keyword == expression.type) CwtConfigTypes.Type else CwtConfigTypes.Subtype
            val nextOffset = offset + prefix.length + rangeInElement.startOffset
            val info = CwtConfigSymbolInfo(keyword, configType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
    }

    private fun collectInfosFromTypeExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        // 尝试从 typeExpression 中获取
        val readWriteAccess = ReadWriteAccess.Read
        val (prefix, suffix) = CwtConfigTextPatterns.definition
        val text = expressionString.removeSurroundingOrNull(prefix, suffix) ?: return
        val expression = ParadoxDefinitionTypeExpression.resolve(text)
        val keywords = mutableSetOf<String>()
        keywords += expression.type
        keywords += expression.subtypes
        val tuples = text.findKeywordsWithTextRanges(keywords)
        if (tuples.isEmpty()) return
        tuples.forEachFast { (keyword, rangeInElement) ->
            val configType = if (keyword == expression.type) CwtConfigTypes.Type else CwtConfigTypes.Subtype
            val nextOffset = offset + prefix.length + rangeInElement.startOffset
            val info = CwtConfigSymbolInfo(keyword, configType.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
    }

    private fun collectInfosFromCommonDataExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val readWriteAccess = ReadWriteAccess.Read
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.enum
            val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@run
            val nextOffset = offset + prefix.length
            val info = CwtConfigSymbolInfo(name, CwtConfigTypes.Enum.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
        run {
            val (prefix, suffix) = CwtConfigTextPatterns.union
            val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@run
            val nextOffset = offset + prefix.length
            val info = CwtConfigSymbolInfo(name, CwtConfigTypes.Union.id, readWriteAccess, nextOffset, element.startOffset, gameType)
            infos += info
        }
        run {
            val patternSet = CwtConfigTextPatternSets.dynamicValueReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val nextOffset = offset + prefix.length
                val info = CwtConfigSymbolInfo(name, CwtConfigTypes.DynamicValue.id, readWriteAccess, nextOffset, element.startOffset, gameType)
                infos += info
            }
        }
        run {
            val patternSet = CwtConfigTextPatternSets.singleAliasReference
            patternSet.forEach f@{ pattern ->
                val (prefix, suffix) = pattern
                val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
                val nextOffset = offset + prefix.length
                val info = CwtConfigSymbolInfo(name, CwtConfigTypes.SingleAlias.id, readWriteAccess, nextOffset, element.startOffset, gameType)
                infos += info
            }
        }
    }

    private fun collectInfosFromAliasDataExpressions(element: CwtStringExpressionElement, infos: MutableList<CwtConfigSymbolInfo>, gameType: ParadoxGameType, expressionString: String, offset: Int) {
        val readWriteAccess = ReadWriteAccess.Read
        val patternSet = CwtConfigTextPatternSets.aliasReference
        patternSet.forEach f@{ pattern ->
            val (prefix, suffix) = pattern
            val name = expressionString.removeSurroundingOrNull(prefix, suffix)?.orNull() ?: return@f
            val nextOffset = offset + prefix.length
            val info = CwtConfigSymbolInfo(name, CwtConfigTypes.Alias.id, readWriteAccess, nextOffset, element.startOffset, gameType)
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
            CwtConfigTypes.Union -> configType
            CwtConfigTypes.DynamicValueType -> configType
            CwtConfigTypes.SingleAlias -> configType
            CwtConfigTypes.Alias, CwtConfigTypes.Modifier, CwtConfigTypes.Trigger, CwtConfigTypes.Effect -> CwtConfigTypes.Alias
            CwtConfigTypes.Macro -> configType
            else -> null
        }
    }

    private fun getSymbolName(text: String, configType: CwtConfigType): String? {
        return when (configType) {
            CwtConfigTypes.Alias, CwtConfigTypes.Modifier, CwtConfigTypes.Trigger, CwtConfigTypes.Effect -> text.removeSurroundingOrNull("alias[", "]")?.orNull()
            else -> CwtConfigManager.getNameByConfigType(text, configType)
        }
    }
}
