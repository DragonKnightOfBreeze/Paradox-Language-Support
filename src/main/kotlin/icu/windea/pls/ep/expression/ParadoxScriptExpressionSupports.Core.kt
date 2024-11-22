package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.highlighting.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.expression.ParadoxScriptExpressionSupport.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.editor.*
import icu.windea.pls.script.psi.*

class ParadoxScriptLocalisationExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Localisation
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxLocalisationSearch.search(expressionText, selector).find()
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.search(expressionText, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

class ParadoxScriptSyncedLocalisationExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Localisation
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxSyncedLocalisationSearch.search(expressionText, selector).find()
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxSyncedLocalisationSearch.search(expressionText, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeSyncedLocalisation(context, result)
    }
}

class ParadoxScriptInlineLocalisationExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.InlineLocalisation
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (expressionText.isLeftQuoted()) return
        val attributesKey = ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if (element.text.isLeftQuoted()) return null //inline string
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive(exact).preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig(), exact)
        return ParadoxSyncedLocalisationSearch.search(expressionText, selector).find()
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        if (element.text.isLeftQuoted()) return emptySet() //specific expression
        val configGroup = config.configGroup
        val project = configGroup.project
        val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return ParadoxLocalisationSearch.search(expressionText, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况
        if (context.quoted) return

        ParadoxCompletionManager.completeLocalisation(context, result)
    }
}

class ParadoxScriptDefinitionExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Definition
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.expression?.value ?: return null
        val selector = selector(project, element).definition().contextSensitive(exact)
        return ParadoxDefinitionSearch.search(expressionText, typeExpression, selector).find()
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configGroup = config.configGroup
        val project = configGroup.project
        val typeExpression = config.expression?.value ?: return emptySet()
        val selector = selector(project, element).definition().contextSensitive()
        return ParadoxDefinitionSearch.search(expressionText, typeExpression, selector).findAll()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeDefinition(context, result)
    }
}

class ParadoxScriptPathReferenceExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.PathReference
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.PATH_REFERENCE_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return expressionText.toVirtualFile(false)?.toPsiFile(project)
        } else {
            //if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expressionText.normalizePath()
            val selector = selector(project, element).file().contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).find()?.toPsiFile(project)
        }
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val configExpression = config.expression ?: return emptySet()
        val configGroup = config.configGroup
        val project = configGroup.project
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            return expressionText.toVirtualFile(false)?.toPsiFile(project).toSingletonSetOrEmpty()
        } else {
            //if(ParadoxPathReferenceExpressionSupport.get(configExpression) == null) return null
            val pathReference = expressionText.normalizePath()
            val selector = selector(project, element).file().contextSensitive()
            return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findAll().mapNotNull { it.toPsiFile(project) }
        }
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completePathReference(context, result)
    }
}

class ParadoxScriptEnumValueExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.EnumValue
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.configGroup
        val enumName = config.expression?.value ?: return
        val attributesKey = when {
            configGroup.enums[enumName] != null -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
            configGroup.complexEnums[enumName] != null -> ParadoxScriptAttributesKeys.COMPLEX_ENUM_VALUE_KEY
            else -> ParadoxScriptAttributesKeys.ENUM_VALUE_KEY
        }
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val enumName = config.expression?.value ?: return null
        val configGroup = config.configGroup
        val project = configGroup.project
        //尝试解析为简单枚举
        val enumConfig = configGroup.enums[enumName]
        if (enumConfig != null) {
            return ParadoxExpressionManager.resolvePredefinedEnumValue(expressionText, enumName, configGroup)
        }
        //尝试解析为复杂枚举
        val complexEnumConfig = configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            val searchScope = complexEnumConfig.searchScopeType
            val selector = selector(project, element).complexEnumValue()
                .withSearchScopeType(searchScope)
            //.contextSensitive(exact) //unnecessary
            val info = ParadoxComplexEnumValueSearch.search(expressionText, enumName, selector).findFirst()
            if (info != null) {
                val readWriteAccess = ReadWriteAccessDetector.Access.Read //usage
                return ParadoxComplexEnumValueElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
            }
        }
        return null
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeEnumValue(context, result)
    }
}

class ParadoxScriptModifierExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Modifier
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.MODIFIER_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxExpressionManager.annotateExpressionByAttributesKey(element, range, attributesKey, holder)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configGroup = config.configGroup
        return ParadoxExpressionManager.resolveModifier(element, expressionText, configGroup)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxModifierManager.completeModifier(context, result)
    }
}

class ParadoxScriptAliasNameExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        val type = config.expression?.type ?: return false
        return type == CwtDataTypes.AliasName || type == CwtDataTypes.AliasKeysField
    }

    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val configGroup = config.configGroup
        val configExpression = config.expression
        val aliasName = configExpression?.value ?: return
        val aliasMap = configGroup.aliasGroups.get(aliasName) ?: return
        val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expressionText, false, aliasName, configGroup) ?: return
        val aliasConfig = aliasMap[aliasSubName]?.first() ?: return
        INSTANCE.annotate(element, rangeInElement, expressionText, holder, aliasConfig)
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val aliasName = config.expression?.value ?: return null
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
        val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expressionText, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
        return ParadoxExpressionManager.resolveExpression(element, rangeInElement, alias, alias.expression, isKey, exact)
    }

    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        val aliasName = config.expression?.value ?: return emptySet()
        val configGroup = config.configGroup
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptySet()
        val aliasSubName = ParadoxExpressionManager.getAliasSubName(element, expressionText, element.text.isLeftQuoted(), aliasName, configGroup)
        val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptySet()
        return ParadoxExpressionManager.multiResolveExpression(element, rangeInElement, alias, alias.expression, isKey)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        val config = context.config ?: return
        val aliasName = config.expression?.value ?: return
        ParadoxCompletionManager.completeAliasName(context, result, aliasName)
    }
}

abstract class ParadoxScriptConstantLikeExpressionSupport : ParadoxScriptExpressionSupport {
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val annotated = annotateByAliasName(element, rangeInElement, holder, config)
        if (annotated) return
        val configExpression = config.expression ?: return
        if (rangeInElement == null) {
            if (element is ParadoxScriptPropertyKey && configExpression.isKey) return //unnecessary
            if (element is ParadoxScriptString && !configExpression.isKey) return //unnecessary
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

    private fun annotateByAliasName(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
        val aliasConfig = when {
            config is CwtPropertyConfig -> config.aliasConfig
            config is CwtAliasConfig -> config
            else -> null
        } ?: return false
        val type = aliasConfig.expression.type
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
}

class ParadoxScriptConstantExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Constant
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return config.resolved().pointer.element
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeConstant(context, result)
    }
}

class ParadoxScriptTemplateExpressionSupport : ParadoxScriptConstantLikeExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.TemplateExpression
    }

    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.configGroup
        return ParadoxExpressionManager.resolveTemplateExpression(element, expressionText, configExpression, configGroup)
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return //排除可能带参数的情况

        ParadoxCompletionManager.completeTemplateExpression(context, result)
    }
}
