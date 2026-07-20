package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.configExpression.suffixes
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxResolutionManager
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors
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

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression?.value ?: return null
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(name, type, selector).find()
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.configExpression?.value ?: return emptyList()
        val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
        return fullNames.flatMap { fullName ->
            val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
            ParadoxDefinitionSearch.searchElement(fullName, type, selector).findAll()
        }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeDefinition(context, result)
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

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.searchNormal(name, selector).find()
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val configGroup = config.configGroup
        val project = configGroup.project
        return fullNames.flatMap { fullName ->
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            ParadoxLocalisationSearch.searchNormal(fullName, selector).findAll()
        }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeLocalisation(context, result)
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

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        if (config.configExpression?.type?.isSuffixAware == true) {
            // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
            return ParadoxExpressionManager.annotateExpressionAsHighlightedReference(range, holder)
        }
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val name = fullNames.singleOrNull() ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.searchSynced(name, selector).find()
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
        val configGroup = config.configGroup
        val project = configGroup.project
        return fullNames.flatMap { fullName ->
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchSynced(fullName, selector).findAll()
        }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.config?.configExpression?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeSyncedLocalisation(context, result)
    }
}

/**
 * @see CwtDataTypes.InlineLocalisation
 */
class ParadoxScriptInlineLocalisationExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.InlineLocalisation
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        if (text.isLeftQuoted()) return
        val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (element.text.isLeftQuoted()) return null // inline string
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.searchNormal(text, selector).find()
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        if (element.text.isLeftQuoted()) return emptyList() // specific expression
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.searchNormal(text, selector).findAll()
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        if (context.globalContext.leftQuoted) return
        ParadoxExpressionCompletionManager.completeLocalisation(context, result)
    }
}

/**
 * @see CwtDataTypes.Modifier
 */
class ParadoxScriptModifierExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Modifier
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val attributesKey = ParadoxScriptHighlighterColors.MODIFIER
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val configGroup = config.configGroup
        return ParadoxResolutionManager.resolveModifier(element, text, configGroup)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeModifier(context, result)
    }
}

/**
 * @see CwtDataTypes.EnumValue
 */
class ParadoxScriptEnumValueExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val configGroup = config.configGroup
        val enumName = config.configExpression?.value ?: return
        val attributesKey = when {
            configGroup.enums[enumName] != null -> ParadoxScriptHighlighterColors.ENUM_VALUE
            configGroup.complexEnums[enumName] != null -> ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE
            else -> ParadoxScriptHighlighterColors.ENUM_VALUE
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return ParadoxResolutionManager.resolveEnumValue(element, text, config)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeEnumValue(context, result)
    }
}

/**
 * @see CwtDataTypes.UnionValue
 */
class ParadoxScriptUnionValueExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.UnionValue
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val configGroup = config.configGroup
        val unionName = config.configExpression?.value ?: return
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedScriptUnionCandidate(element, expression, unionName, configGroup) ?: return
        ParadoxExpressionService.annotateScriptExpression(element, rangeInElement, text, valueConfig, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val configGroup = config.configGroup
        val unionName = config.configExpression?.value ?: return null
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedScriptUnionCandidate(element, expression, unionName, configGroup) ?: return null
        return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, valueConfig, role)
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val configGroup = config.configGroup
        val unionName = config.configExpression?.value ?: return emptyList()
        val quoted = element.text.isLeftQuoted()
        val expression = ParadoxExpression.resolve(text, quoted)
        val valueConfig = ParadoxExpressionMatchService.getMatchedScriptUnionCandidate(element, expression, unionName, configGroup) ?: return emptyList()
        return ParadoxExpressionManager.resolveAllScriptExpression(element, rangeInElement, valueConfig, role)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // if (context.keyword.isParameterized()) return // 2.2.0 兼容可能带参数的情况
        ParadoxExpressionCompletionManager.completeScriptUnionValue(context, result)
    }
}

/**
 * @see CwtDataTypes.AliasName
 * @see CwtDataTypes.AliasKeysField
 */
class ParadoxScriptAliasNameExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val configGroup = config.configGroup
        val configExpression = config.configExpression
        val aliasName = configExpression?.value ?: return
        val aliasGroup = configGroup.aliasGroups.get(aliasName) ?: return
        val quoted = element.text.isLeftQuoted()
        val aliasExpression = ParadoxExpression.resolve(text, quoted)
        val aliasSubName = ParadoxExpressionMatchService.getMatchedAliasKey(element, aliasExpression, aliasName, configGroup) ?: return
        val aliasConfig = aliasGroup[aliasSubName]?.first() ?: return
        ParadoxExpressionService.annotateScriptExpression(element, rangeInElement, text, aliasConfig, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        val aliasName = config.configExpression?.value ?: return null
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
        val quoted = element.text.isLeftQuoted()
        val aliasExpression = ParadoxExpression.resolve(text, quoted, role)
        val aliasSubName = ParadoxExpressionMatchService.getMatchedAliasKey(element, aliasExpression, aliasName, configGroup) ?: return null
        val aliasConfig = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
        return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, aliasConfig, role)
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val aliasName = config.configExpression?.value ?: return emptyList()
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
        val quoted = element.text.isLeftQuoted()
        val aliasExpression = ParadoxExpression.resolve(text, quoted, role)
        val aliasSubName = ParadoxExpressionMatchService.getMatchedAliasKey(element, aliasExpression, aliasName, configGroup) ?: return emptyList()
        val aliasConfig = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptyList()
        return ParadoxExpressionManager.resolveAllScriptExpression(element, rangeInElement, aliasConfig, role)
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        // if (context.keyword.isParameterized()) return // 2.2.0 兼容可能带参数的情况
        ParadoxExpressionCompletionManager.completeAliasName(context, result)
    }
}

/**
 * @see CwtDataTypes.Constant
 */
class ParadoxScriptConstantExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Constant
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val annotated = annotateByAliasName(element, rangeInElement, holder, config)
        if (annotated) return
        val configExpression = config.configExpression ?: return
        if (rangeInElement == null) {
            if (element is ParadoxScriptPropertyKey && configExpression.isKey) return // unnecessary
            if (element is ParadoxScriptString && !configExpression.isKey) return // unnecessary
        }
        val attributesKey = when {
            configExpression.isKey -> ParadoxScriptHighlighterColors.PROPERTY_KEY
            else -> ParadoxScriptHighlighterColors.STRING
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
        if (type !in CwtDataTypeSets.ConstantAware) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptHighlighterColors.MODIFIER
            aliasName == "trigger" -> ParadoxScriptHighlighterColors.TRIGGER
            aliasName == "effect" -> ParadoxScriptHighlighterColors.EFFECT
            else -> return false
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
        return true
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return config.resolved().pointer.element
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeConstant(context, result)
    }
}

/**
 * @see CwtDataTypeSets.PathReference
 */
class ParadoxScriptPathReferenceExpressionSupport : ParadoxScriptExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.PathReference
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, holder: AnnotationHolder) {
        val attributesKey = ParadoxScriptHighlighterColors.PATH_REFERENCE
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (text.isEmpty()) return null

        val configExpression = config.configExpression ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project

        // absolute file path -> use `VfsUtil.findFile`
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) return text.toVirtualFile()?.toPsiFile(project)

        val pathReference = text.normalizePath()
        if (pathReference.isEmpty()) return null
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
    }

    override fun resolveAll(element: ParadoxExpressionElement, rangeInElement: TextRange?, text: String, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        val configExpression = config.configExpression ?: return emptyList()
        val configGroup = config.configGroup
        val project = configGroup.project

        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return text.toVirtualFile()?.toPsiFile(project).to.singletonListOrEmpty()
        }

        val pathReference = text.normalizePath()
        if (pathReference.isEmpty()) return emptyList()
        val selector = ParadoxFilePathSearch.selector(project, element).contextSensitive()
        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completePathReference(context, result)
    }
}
