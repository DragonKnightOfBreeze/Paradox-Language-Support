package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.inspections.script.common.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

object ParadoxLocalisationCodeInsightContextBuilder {
    fun fromFile(
        file: PsiFile,
        locales: Collection<CwtLocalisationLocaleConfig>,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return null
        val children = mutableListOf<ParadoxLocalisationCodeInsightContext>()
        when (file) {
            is ParadoxScriptFile -> {
                file.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        when (element) {
                            is ParadoxScriptDefinitionElement -> fromDefinition(element, locales, fromInspection = fromInspection)?.let { children.add(it) }
                            is ParadoxScriptStringExpressionElement -> fromExpression(element, locales, fromInspection = fromInspection)?.let { children.add(it) }
                        }
                        if (!ParadoxPsiManager.inMemberContext(element)) return //optimize
                        super.visitElement(element)
                    }
                })
            }
            is ParadoxLocalisationFile -> {
                file.accept(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is ParadoxLocalisationProperty) fromLocalisation(element, locales, fromInspection = fromInspection)?.let { children.add(it) }
                        if (!ParadoxPsiManager.inLocalisationContext(element)) return //optimize
                        super.visitElement(element)
                    }
                })
            }
        }
        if (children.isEmpty()) return null
        //exclude duplicates and sort contexts
        val finalChildren = children
            .distinctBy { it.type.name + "@" + it.name }
            .sortedWith(compareBy({ it.type }, { if (isLocalisationType(it.type)) it.name else 0 }))
        return ParadoxLocalisationCodeInsightContext(Type.File, file.name, emptyList(), finalChildren, fromInspection)
    }

    fun isLocalisationType(type: Type): Boolean {
        return type == Type.LocalisationReference || type == Type.SyncedLocalisationReference || type == Type.Localisation
    }

    fun fromDefinition(
        element: ParadoxScriptDefinitionElement,
        locales: Collection<CwtLocalisationLocaleConfig>,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        val inspection = if (fromInspection) getMissingLocalisationInspection(element) else null

        if (!(inspection == null || inspection.checkForDefinitions)) return null
        val definitionInfo = element.definitionInfo ?: return null
        val project = definitionInfo.project
        val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()

        for (info in definitionInfo.localisations) {
            ProgressManager.checkCanceled()
            val expression = info.locationExpression
            for (locale in locales) {
                ProgressManager.checkCanceled()
                val resolveResult = CwtLocationExpressionManager.resolve(expression, element, definitionInfo) { locale(locale)}
                val type = when {
                    info.required -> ParadoxLocalisationCodeInsightInfo.Type.Required
                    info.primary -> ParadoxLocalisationCodeInsightInfo.Type.Primary
                    else -> ParadoxLocalisationCodeInsightInfo.Type.Optional
                }
                val name = resolveResult?.name
                val check = when {
                    info.required -> true
                    checkPrimaryForDefinitions(inspection) && (info.primary || info.primaryByInference) -> true
                    checkOptionalForDefinitions(inspection) && !info.required -> true
                    else -> false
                }
                val missing = resolveResult?.element == null && resolveResult?.message == null
                val dynamic = resolveResult?.message != null
                val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, info, locale, check, missing, dynamic)
                codeInsightInfos += codeInsightInfo
            }
        }

        for (info in definitionInfo.modifiers) {
            ProgressManager.checkCanceled()
            val modifierName = info.name
            run {
                val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierName
                val check = checkGeneratedModifierNamesForDefinitions(inspection)
                val keys = ParadoxModifierManager.getModifierNameKeys(modifierName, element)
                val keyToUse = keys.firstOrNull() ?: return@run
                for (locale in locales) {
                    ProgressManager.checkCanceled()
                    val missing = keys.all { key -> isMissing(key, project, element, locale) }
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            run {
                val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierDesc
                val check = checkGeneratedModifierDescriptionsForDefinitions(inspection)
                val keys = ParadoxModifierManager.getModifierDescKeys(modifierName, element)
                val keyToUse = keys.firstOrNull() ?: return@run
                for (locale in locales) {
                    ProgressManager.checkCanceled()
                    val missing = keys.all { key -> isMissing(key, project, element, locale) }
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
        }

        return ParadoxLocalisationCodeInsightContext(Type.Definition, definitionInfo.name, codeInsightInfos, fromInspection = fromInspection)
    }

    fun fromLocalisation(
        element: ParadoxLocalisationProperty,
        locales: Collection<CwtLocalisationLocaleConfig>,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        val inspectionState = if (fromInspection) checkForLocalisations(element) else true
        if (!inspectionState) return null

        val contextType = Type.Localisation
        val type = ParadoxLocalisationCodeInsightInfo.Type.Primary
        val name = element.name
        if (name.isEmpty()) return null
        val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
        val project by lazy { element.project }
        for (locale in locales) {
            ProgressManager.checkCanceled()
            val missing = isMissing(name, project, element, locale)
            val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, true, missing, false)
            codeInsightInfos += codeInsightInfo
        }
        return ParadoxLocalisationCodeInsightContext(contextType, name, codeInsightInfos, fromInspection = fromInspection)
    }

    fun fromExpression(
        element: ParadoxScriptStringExpressionElement,
        locales: Collection<CwtLocalisationLocaleConfig>,
        forReference: Boolean = true,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        if (!element.isExpression()) return null
        val expression = element.value
        if (expression.isEmpty() || expression.isParameterized()) return null
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return null
        fromModifier(element, config, locales, fromInspection = fromInspection)?.let { return it }
        if (forReference) {
            fromReference(element, config, locales, fromInspection = fromInspection)?.let { return it }
        }
        return null
    }

    fun fromModifier(
        element: ParadoxScriptStringExpressionElement,
        config: CwtMemberConfig<*>,
        locales: Collection<CwtLocalisationLocaleConfig>,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        val inspection = if (fromInspection) getMissingLocalisationInspection(element) else null

        if (!(inspection == null || inspection.checkForModifiers)) return null
        if (config.expression.type != CwtDataTypes.Modifier) return null
        val modifierName = element.value
        val project = config.configGroup.project
        val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()

        run {
            val type = ParadoxLocalisationCodeInsightInfo.Type.ModifierName
            val check = checkModifierNames(inspection)
            val keys = ParadoxModifierManager.getModifierNameKeys(modifierName, element)
            val keyToUse = keys.firstOrNull() ?: return@run
            for (locale in locales) {
                ProgressManager.checkCanceled()
                val missing = keys.all { key -> isMissing(key, project, element, locale) }
                val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                codeInsightInfos += codeInsightInfo
            }
        }
        run {
            val type = ParadoxLocalisationCodeInsightInfo.Type.ModifierDesc
            val check = checkModifierDescriptions(inspection)
            val keys = ParadoxModifierManager.getModifierDescKeys(modifierName, element)
            val keyToUse = keys.firstOrNull() ?: return@run
            for (locale in locales) {
                ProgressManager.checkCanceled()
                val missing = keys.all { key -> isMissing(key, project, element, locale) }
                val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                codeInsightInfos += codeInsightInfo
            }
        }

        return ParadoxLocalisationCodeInsightContext(Type.Modifier, modifierName, codeInsightInfos, fromInspection = fromInspection)
    }

    fun fromReference(
        element: ParadoxScriptStringExpressionElement,
        config: CwtMemberConfig<*>,
        locales: Collection<CwtLocalisationLocaleConfig>,
        fromInspection: Boolean = false,
    ): ParadoxLocalisationCodeInsightContext? {
        val inspectionState = if (fromInspection) checkForReferences(element) else true
        if (!inspectionState) return null

        val contextType = when {
            config.expression.type == CwtDataTypes.Localisation -> Type.LocalisationReference
            config.expression.type == CwtDataTypes.SyncedLocalisation -> Type.SyncedLocalisationReference
            config.expression.type == CwtDataTypes.InlineLocalisation && !element.text.isLeftQuoted() -> Type.LocalisationReference
            else -> null
        }
        if (contextType == null) return null
        val type = ParadoxLocalisationCodeInsightInfo.Type.Reference
        val name = element.value
        if (name.isEmpty()) return null
        val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
        val project by lazy { element.project }
        for (locale in locales) {
            ProgressManager.checkCanceled()
            val missing = isMissing(name, project, element, locale)
            val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, true, missing, false)
            codeInsightInfos += codeInsightInfo
        }
        return ParadoxLocalisationCodeInsightContext(contextType, name, codeInsightInfos, fromInspection = fromInspection)
    }

    private fun isMissing(name: String, project: Project, context: PsiElement, locale: CwtLocalisationLocaleConfig): Boolean {
        val selector = selector(project, context).localisation().locale(locale)
        val missing = ParadoxLocalisationSearch.search(name, selector).findFirst() == null
        return missing
    }

    private fun getMissingLocalisationInspection(context: PsiElement): MissingLocalisationInspection? {
        return getInspectionToolState("ParadoxScriptMissingLocalisation", context, context.project)?.enabledTool?.castOrNull()
    }

    private fun checkPrimaryForDefinitions(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || inspection.checkForDefinitions
    }

    private fun checkOptionalForDefinitions(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || inspection.checkOptionalForDefinitions
    }

    private fun checkGeneratedModifierNamesForDefinitions(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || (inspection.checkGeneratedModifiersForDefinitions && inspection.checkGeneratedModifierNamesForDefinitions)
    }

    private fun checkGeneratedModifierDescriptionsForDefinitions(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || (inspection.checkGeneratedModifiersForDefinitions && inspection.checkGeneratedModifierDescriptionsForDefinitions)
    }

    private fun checkModifierNames(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || (inspection.checkForModifiers && inspection.checkModifierNames)
    }

    private fun checkModifierDescriptions(inspection: MissingLocalisationInspection?) : Boolean {
        return inspection == null || (inspection.checkForModifiers && inspection.checkModifierDescriptions)
    }

    private fun checkForLocalisations(context: PsiElement): Boolean {
        if (context.language !is ParadoxLocalisationLanguage) return true
        val state = getInspectionToolState("ParadoxLocalisationMissingLocalisation", context, context.project) ?: return true
        return state.isEnabled
    }

    private fun checkForReferences(context: PsiElement): Boolean {
        if (context.language !is ParadoxScriptLanguage) return true
        val state = getInspectionToolState("ParadoxScriptUnresolvedExpression", context, context.project) ?: return true
        return state.isEnabled
    }
}
