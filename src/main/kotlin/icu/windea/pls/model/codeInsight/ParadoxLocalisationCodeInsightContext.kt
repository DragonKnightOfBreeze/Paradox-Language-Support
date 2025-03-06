package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.inspections.script.common.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

data class ParadoxLocalisationCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxLocalisationCodeInsightInfo>,
    val children: List<ParadoxLocalisationCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        LocalisationReference,
        SyncedLocalisationReference
    }

    companion object {
        private fun getMissingLocalisationInspection(context: PsiElement): MissingLocalisationInspection? {
            return getInspectionToolState("ParadoxScriptMissingLocalisation", context, context.project)?.enabledTool?.castOrNull()
        }

        private fun getUnresolvedExpressionInspectionState(context: PsiElement): Boolean {
            return getInspectionToolState("ParadoxScriptUnresolvedExpression", context, context.project)?.isEnabled ?: false
        }

        fun fromFile(
            file: PsiFile,
            locales: Collection<CwtLocalisationLocaleConfig>,
            fromInspection: Boolean = false,
        ): ParadoxLocalisationCodeInsightContext? {
            if (file !is ParadoxScriptFile) return null
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            val children = mutableListOf<ParadoxLocalisationCodeInsightContext>()
            file.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    when (element) {
                        is ParadoxScriptDefinitionElement -> fromDefinition(element, locales, fromInspection = fromInspection)?.let { children.add(it) }
                        is ParadoxScriptStringExpressionElement -> fromExpression(element, locales, fromInspection = fromInspection)?.let { children.add(it) }
                    }
                    if (element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
            //exclude duplicates and sort contexts
            val finalChildren = children
                .distinctBy { it.type.name + "@" + it.name }
                .sortedWith(compareBy({ it.type }, { if (it.type == Type.LocalisationReference || it.type == Type.SyncedLocalisationReference) it.name else 0 }))
            return ParadoxLocalisationCodeInsightContext(Type.File, file.name, codeInsightInfos, finalChildren, fromInspection)
        }

        fun fromDefinition(
            definition: ParadoxScriptDefinitionElement,
            locales: Collection<CwtLocalisationLocaleConfig>,
            fromInspection: Boolean = false,
        ): ParadoxLocalisationCodeInsightContext? {
            val inspection = if (fromInspection) getMissingLocalisationInspection(definition) else null

            if (!(inspection == null || inspection.checkForDefinitions)) return null
            val definitionInfo = definition.definitionInfo ?: return null
            val project = definitionInfo.project
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()

            for (info in definitionInfo.localisations) {
                ProgressManager.checkCanceled()
                val expression = info.locationExpression
                for (locale in locales) {
                    ProgressManager.checkCanceled()
                    val selector = selector(project, definition).localisation().locale(locale) //use file as context
                    val resolved = expression.resolve(definition, definitionInfo, selector)
                    val type = when {
                        info.required -> ParadoxLocalisationCodeInsightInfo.Type.Required
                        info.primary -> ParadoxLocalisationCodeInsightInfo.Type.Primary
                        else -> ParadoxLocalisationCodeInsightInfo.Type.Optional
                    }
                    val name = resolved?.name
                    val check = when {
                        info.required -> true
                        (inspection == null || inspection.checkPrimaryForDefinitions) && (info.primary || info.primaryByInference) -> true
                        (inspection == null || inspection.checkOptionalForDefinitions) && !info.required -> true
                        else -> false
                    }
                    val missing = resolved?.element == null && resolved?.message == null
                    val dynamic = resolved?.message != null
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, info, locale, check, missing, dynamic)
                    codeInsightInfos += codeInsightInfo
                }
            }

            for (info in definitionInfo.modifiers) {
                ProgressManager.checkCanceled()
                val modifierName = info.name
                run {
                    val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierName
                    val check = inspection == null || inspection.checkGeneratedModifierNamesForDefinitions
                    val keys = ParadoxModifierManager.getModifierNameKeys(modifierName, definition)
                    val keyToUse = keys.firstOrNull() ?: return@run
                    for (locale in locales) {
                        ProgressManager.checkCanceled()
                        val missing = keys.all { key -> isMissing(key, project, definition, locale) }
                        val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                        codeInsightInfos += codeInsightInfo
                    }
                }
                run {
                    val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierDesc
                    val check = inspection == null || inspection.checkGeneratedModifierDescriptionsForDefinitions
                    val keys = ParadoxModifierManager.getModifierDescKeys(modifierName, definition)
                    val keyToUse = keys.firstOrNull() ?: return@run
                    for (locale in locales) {
                        ProgressManager.checkCanceled()
                        val missing = keys.all { key -> isMissing(key, project, definition, locale) }
                        val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, keyToUse, null, locale, check, missing, false)
                        codeInsightInfos += codeInsightInfo
                    }
                }
            }

            return ParadoxLocalisationCodeInsightContext(Type.Definition, definitionInfo.name, codeInsightInfos, fromInspection = fromInspection)
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
                val check = inspection == null || inspection.checkModifierNames
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
                val check = inspection == null || inspection.checkModifierDescriptions
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
            val inspectionState = if (fromInspection) getUnresolvedExpressionInspectionState(element) else null

            if (!(inspectionState == null || inspectionState)) return null
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
    }
}
