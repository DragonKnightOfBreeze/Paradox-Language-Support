package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.resolved
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.resolve.ParadoxScriptExpressionService
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScopeType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptString

// Core

/**
 * @see CwtDataTypes.Definition
 * @see CwtDataTypes.SuffixAwareDefinition
 */
class ParadoxScriptDefinitionExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Definition || dataType == CwtDataTypes.SuffixAwareDefinition
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression?.value ?: return null
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = selector(project, element).definition().contextSensitive(exact)
        return ParadoxDefinitionSearch.search(name, type, selector).find()
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression?.value ?: return emptySet()
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        return fullNames.flatMap { fullName ->
            val selector = selector(project, element).definition().contextSensitive()
            ParadoxDefinitionSearch.search(fullName, type, selector).findAll()
        }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeDefinition(context, result)
    }
}

/**
 * @see CwtDataTypes.Localisation
 * @see CwtDataTypes.SuffixAwareLocalisation
 */
class ParadoxScriptLocalisationExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Localisation || dataType == CwtDataTypes.SuffixAwareLocalisation
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val configGroup = config.configGroup
        val project = configGroup.project
        return fullNames.flatMap { fullName ->
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            ParadoxLocalisationSearch.searchNormal(fullName, selector).findAll()
        }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

/**
 * @see CwtDataTypes.SyncedLocalisation
 * @see CwtDataTypes.SuffixAwareSyncedLocalisation
 */
class ParadoxScriptSyncedLocalisationExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.SyncedLocalisation || dataType == CwtDataTypes.SuffixAwareSyncedLocalisation
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxLocalisationSearch.searchSynced(name, selector).find()
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val fullNames = ParadoxExpressionManager.getFullNamesFromSuffixAware(expressionText, config)
        val configGroup = config.configGroup
        val project = configGroup.project
        return fullNames.flatMap { fullName ->
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchSynced(fullName, selector).findAll()
        }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeSyncedLocalisation(context, result)
    }
}

/**
 * @see CwtDataTypes.InlineLocalisation
 */
class ParadoxScriptInlineLocalisationExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.InlineLocalisation
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (expressionText.isLeftQuoted()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element.text.isLeftQuoted()) return null // inline string
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxLocalisationSearch.searchNormal(expressionText, selector).find()
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        if (element.text.isLeftQuoted()) return emptySet() // specific expression
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.searchNormal(expressionText, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        if (context.quoted) return

        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

/**
 * @see CwtDataTypeGroups.PathReference
 */
class ParadoxScriptPathReferenceExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeGroups.PathReference
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.configExpression ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return expressionText.toVirtualFile()?.toPsiFile(project)
        } else {
            // if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expressionText.normalizePath()
            val selector = selector(project, element).file().contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
        }
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configExpression = config.configExpression ?: return emptySet()
        val configGroup = config.configGroup
        val project = configGroup.project
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return expressionText.toVirtualFile()?.toPsiFile(project).singleton.setOrEmpty()
        } else {
            // if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expressionText.normalizePath()
            val selector = selector(project, element).file().contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
        }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completePathReference(context, result)
    }
}

/**
 * @see CwtDataTypes.EnumValue
 */
class ParadoxScriptEnumValueExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.configGroup
        val enumName = config.configExpression?.value ?: return
        val attributesKey = when {
            configGroup.enums[enumName] != null -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
            configGroup.complexEnums[enumName] != null -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val enumName = config.configExpression?.value ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        // 尝试解析为简单枚举
        val enumConfig = configGroup.enums[enumName]
        if (enumConfig != null) {
            return ParadoxExpressionManager.resolvePredefinedEnumValue(expressionText, enumName, configGroup)
        }
        // 尝试解析为复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            val searchScopeType = complexEnumConfig.searchScopeType
            val selector = selector(project, element).complexEnumValue()
                .withSearchScopeType(searchScopeType)
            // .contextSensitive(exact) // unnecessary
            val info = ParadoxComplexEnumValueSearch.search(expressionText, enumName, selector).findFirst()
            if (info != null) {
                val readWriteAccess = ReadWriteAccessDetector.Access.Read // usage
                return ParadoxComplexEnumValueElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
            }
        }
        return null
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeEnumValue(context, result)
    }
}

/**
 * @see CwtDataTypes.Modifier
 */
class ParadoxScriptModifierExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Modifier
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.MODIFIER_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.configGroup
        return ParadoxExpressionManager.resolveModifier(element, expressionText, configGroup)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeModifier(context, result)
    }
}

/**
 * @see CwtDataTypes.AliasName
 * @see CwtDataTypes.AliasKeysField
 */
class ParadoxScriptAliasNameExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.AliasName || dataType == CwtDataTypes.AliasKeysField
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.configGroup
        val configExpression = config.configExpression
        val aliasName = configExpression?.value ?: return
        val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
        val aliasSubName = ParadoxConfigMatchService.getMatchedAliasKey(configGroup, aliasName, expressionText, element, false) ?: return
        val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
        ParadoxScriptExpressionService.annotate(element, rangeInElement, expressionText, holder, aliasConfig)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val aliasName = config.configExpression?.value ?: return null
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasSubName = ParadoxConfigMatchService.getMatchedAliasKey(configGroup, aliasName, expressionText, element, element.text.isLeftQuoted())
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
        return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, alias, alias.configExpression, isKey, exact)
    }

    override fun multiResolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val aliasName = config.configExpression?.value ?: return emptySet()
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptySet()
        val aliasSubName = ParadoxConfigMatchService.getMatchedAliasKey(configGroup, aliasName, expressionText, element, element.text.isLeftQuoted())
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptySet()
        return ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, alias, alias.configExpression, isKey)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        val config = context.config ?: return
        val aliasName = config.configExpression?.value ?: return
        ParadoxCompletionManager.completeAliasName(context, result, aliasName)
    }
}

/**
 * @see CwtDataTypes.Constant
 */
class ParadoxScriptConstantExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Constant
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val annotated = annotateByAliasName(element, rangeInElement, holder, config)
        if (annotated) return
        val configExpression = config.configExpression ?: return
        if (rangeInElement == null) {
            if (element is ParadoxScriptPropertyKey && configExpression.isKey) return // unnecessary
            if (element is ParadoxScriptString && !configExpression.isKey) return // unnecessary
        }
        val attributesKey = when {
            configExpression.isKey -> ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY
            else -> ParadoxScriptAttributesKeys.STRING_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (range.isEmpty) return
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    private fun annotateByAliasName(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
        val aliasConfig = when {
            config is CwtPropertyConfig -> config.aliasConfig
            config is CwtAliasConfig -> config
            else -> null
        } ?: return false
        val type = aliasConfig.configExpression.type
        if (type !in CwtDataTypeGroups.ConstantLike) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptAttributesKeys.MODIFIER_KEY
            aliasName == "trigger" -> ParadoxScriptAttributesKeys.TRIGGER_KEY
            aliasName == "effect" -> ParadoxScriptAttributesKeys.EFFECT_KEY
            else -> return false
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
        return true
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.resolved().pointer.element
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况

        ParadoxCompletionManager.completeConstant(context, result)
    }
}
