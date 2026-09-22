package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.highlighting.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.ParadoxLocalisationParameterService
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

abstract class ParadoxCoreScriptExpressionSupport : ParadoxScriptExpressionSupport {
    /**
     * @see CwtDataTypes.Definition
     * @see CwtDataTypes.SuffixAwareDefinition
     */
    class ForDefinition : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Definition || dataType == CwtDataTypes.SuffixAwareDefinition
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val typeExpression = config.configExpression?.metadata?.value ?: return null
            val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
            val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.searchElement(name, type, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            val typeExpression = config.configExpression?.metadata?.value ?: return emptyList()
            val type = typeExpression.substringBefore('.') // 匹配和解析定义时忽略子类型
            return fullNames.flatMap { fullName ->
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
                ParadoxDefinitionSearch.searchElement(fullName, type, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeDefinition(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Localisation
     * @see CwtDataTypes.SuffixAwareLocalisation
     */
    class ForLocalisation : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Localisation || dataType == CwtDataTypes.SuffixAwareLocalisation
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(name, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            return fullNames.flatMap { fullName ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                ParadoxLocalisationSearch.searchNormal(fullName, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.SyncedLocalisation
     * @see CwtDataTypes.SuffixAwareSyncedLocalisation
     */
    class ForSyncedLocalisation : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.SyncedLocalisation || dataType == CwtDataTypes.SuffixAwareSyncedLocalisation
        }

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            if (config.configExpression?.type?.isSuffixAware == true) {
                // 使用特殊的高亮（HIGHLIGHTED_REFERENCE）
                ParadoxExpressionSupportFactory.annotateExpressionAsHighlightedReference(element, rangeInExpression, holder)
            } else {
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val name = fullNames.singleOrNull() ?: return null
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchSynced(name, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            val fullNames = CwtConfigManager.getFullNamesFromSuffixAware(config, text)
            val configGroup = config.configGroup
            val project = configGroup.project
            return fullNames.flatMap { fullName ->
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                return ParadoxLocalisationSearch.searchSynced(fullName, selector).findAll()
            }
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.config?.configExpression?.metadata?.suffixes.isNotNullOrEmpty()) return // TODO SUFFIX_AWARE 排除需要带上后缀的情况，目前不支持
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeSyncedLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.InlineLocalisation
     */
    class ForInlineLocalisation : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.InlineLocalisation

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            if (text.isLeftQuoted()) return false
            val attributesKey = ParadoxScriptHighlighterColors.LOCALISATION_REFERENCE
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            if (element.text.isLeftQuoted()) return null // inline string
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(text, selector).find()
        }

        override fun resolveAll(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
            if (element.text.isLeftQuoted()) return emptyList() // specific expression
            val configGroup = config.configGroup
            val project = configGroup.project
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            return ParadoxLocalisationSearch.searchNormal(text, selector).findAll()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            if (context.leftQuoted) return
            ParadoxExpressionCompletionManager.completeLocalisation(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Modifier
     */
    class ForModifier : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Modifier

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val attributesKey = ParadoxScriptHighlighterColors.MODIFIER
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            val configGroup = config.configGroup
            return ParadoxExpressionSupportFactory.resolveModifier(element, text, configGroup)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeModifier(context, result)
        }
    }

    /**
     * @see CwtDataTypes.EnumValue
     */
    class ForEnumValue : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.EnumValue

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            val configGroup = config.configGroup
            val enumName = config.configExpression?.metadata?.value ?: return false
            val attributesKey = when {
                configGroup.enums[enumName] != null -> ParadoxScriptHighlighterColors.ENUM_VALUE
                configGroup.complexEnums[enumName] != null -> ParadoxScriptHighlighterColors.COMPLEX_ENUM_VALUE
                else -> ParadoxScriptHighlighterColors.ENUM_VALUE
            }
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            return ParadoxExpressionSupportFactory.resolveEnumValue(element, text, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeEnumValue(context, result)
        }
    }

    /**
     * @see CwtDataTypes.TechnologyWithLevel
     */
    @ForGameType(ParadoxGameType.Stellaris)
    class ForTechnologyWithLevel : ParadoxCoreScriptExpressionSupport() {
        // https://github.com/cwtools/cwtools-vscode/issues/58

        private val typeExpression = "<technology.repeatable>"

        override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.TechnologyWithLevel

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            if (element !is ParadoxScriptStringExpressionElement) return false // only for string expressions in script files
            val separatorIndex = text.indexOf('@')
            if (separatorIndex == -1) return false
            run {
                val offset = separatorIndex
                if (offset <= 0) return@run
                val attributesKey = ParadoxScriptHighlighterColors.DEFINITION_REFERENCE
                val rangeInExpression = TextRange.create(rangeInExpression.startOffset, rangeInExpression.startOffset + offset)
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            run {
                val offset = separatorIndex
                val attributesKey = ParadoxScriptHighlighterColors.SEMANTIC_MARKER
                val rangeInExpression = TextRange.create(rangeInExpression.startOffset + offset, rangeInExpression.startOffset + offset + 1)
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            run {
                val offset = text.length - separatorIndex - 1
                if (offset <= 0) return@run
                // annotate only if snippet after '@' is number like
                if (!text.substring(separatorIndex + 1).all { it.isExactDigit() }) return@run
                val attributesKey = ParadoxScriptHighlighterColors.NUMBER
                val rangeInExpression = TextRange.create(rangeInExpression.endOffset - offset, rangeInExpression.endOffset)
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            }
            return true
        }

        override fun getReferences(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiReference> {
            if (element !is ParadoxScriptStringExpressionElement) return emptyList()
            val separatorIndex = text.indexOf('@')
            if (separatorIndex == -1) return emptyList() // no `@` -> ignore
            if (separatorIndex == 0) return emptyList() // no tech node -> ignore
            val offset = ParadoxExpressionService.getExpressionOffset(element)
            val referenceRange = TextRange.from(rangeInExpression.startOffset + offset, separatorIndex)
            val referenceConfigs = listOf(CwtValueConfig.mock(config.configGroup, typeExpression))
            val referenceRole = ParadoxExpressionRole.Other
            val reference = ParadoxScriptExpressionPsiReference(element, referenceRange, referenceConfigs, referenceRole)
            return reference.to.singletonList()
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            val definitionScriptExpressionSupport = ParadoxScriptExpressionSupport.EP_NAME.findExtension(ForDefinition::class.java) ?: return

            val separatorIndex = context.keyword.indexOf('@')
            if (separatorIndex != -1 && context.keywordOffset - separatorIndex > 0) return

            val config = CwtValueConfig.mock(context.configGroup, typeExpression)
            val context = context.copy(isKey = null, config = config, configs = emptySet())
            definitionScriptExpressionSupport.complete(context, result)
        }
    }

    /**
     * @see CwtDataTypes.Parameter
     */
    class ForParameter : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Parameter

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            if (element !is ParadoxScriptStringExpressionElement) return false // only for string expressions in script files
            val attributesKey = ParadoxSemanticHighlighterColors.argument()
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            if (element !is ParadoxScriptStringExpressionElement) return null // only for string expressions in script files
            return ParadoxParameterService.resolveArgument(element, rangeInExpression, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            if (context.keyword.isParameterized()) return // 排除可能带参数的情况
            ParadoxExpressionCompletionManager.completeArgument(context, result)
        }
    }

    /**
     * @see CwtDataTypes.LocalisationParameter
     */
    class ForLocalisationParameter : ParadoxCoreScriptExpressionSupport() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.LocalisationParameter

        override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
            if (element !is ParadoxScriptStringExpressionElement) return false // only for string expressions in script files
            val attributesKey = ParadoxSemanticHighlighterColors.argument()
            ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
            return true
        }

        override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
            if (element !is ParadoxScriptStringExpressionElement) return null // only for string expressions in script files
            return ParadoxLocalisationParameterService.resolveArgument(element, rangeInExpression, config)
        }

        override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
            // NOTE 不兼容本地化参数（CwtDataTypes.LocalisationParameter），因为那个引用实际上也可能对应一个缺失的本地化的名字
        }
    }
}
