package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

data class ParadoxLocalisationCodeInsightInfo(
    val type: Type,
    val name: String?,
    val relatedLocalisationInfo: ParadoxDefinitionRelatedLocalisationInfo?,
    val locale: CwtLocalisationLocaleConfig,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierName, GeneratedModifierDesc,
        ModifierName, ModifierDesc
    }
    
    val key = when {
        relatedLocalisationInfo != null -> "@${relatedLocalisationInfo.key}@${locale.id}"
        name != null -> "$name@${locale.id}"
        else -> null
    }
    
    companion object {
        fun fromDefinition(
            definition: ParadoxScriptDefinitionElement,
            locales: Set<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection? = null
        ): List<ParadoxLocalisationCodeInsightInfo>? {
            if(!(inspection == null || inspection.checkForDefinitions)) return null
            val definitionInfo = definition.definitionInfo ?: return null
            val project = definitionInfo.project
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            
            for(info in definitionInfo.localisations) {
                ProgressManager.checkCanceled()
                val expression = info.locationExpression
                for(locale in locales) {
                    ProgressManager.checkCanceled()
                    val selector = localisationSelector(project, definition).locale(locale) //use file as context
                    val resolved = expression.resolve(definition, definitionInfo, selector)
                    val type = when {
                        info.required -> Type.Required
                        info.primary -> Type.Primary
                        else -> Type.Optional
                    }
                    val name = resolved?.name
                    val check = when {
                        info.required -> true
                        (inspection == null || inspection.checkPrimaryForDefinitions) && (info.primary || info.primaryByInference) -> true
                        (inspection == null || inspection.checkOptionalForDefinitions) && !info.required -> true
                        else -> false
                    }
                    val missing = resolved?.localisation == null && resolved?.message == null
                    val dynamic = resolved?.message != null
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, info, locale, check, missing, dynamic)
                    codeInsightInfos += codeInsightInfo
                }
            }
            
            for(info in definitionInfo.modifiers) {
                ProgressManager.checkCanceled()
                val modifierName = info.name
                run {
                    val type = Type.GeneratedModifierName
                    val check = inspection == null || inspection.checkGeneratedModifierNamesForDefinitions
                    val name = ParadoxModifierHandler.getModifierNameKey(modifierName)
                    for(locale in locales) {
                        ProgressManager.checkCanceled()
                        val missing = isMissing(name, project, definition, locale)
                        val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, check, missing, false)
                        codeInsightInfos += codeInsightInfo
                    }
                }
                run {
                    val type = Type.GeneratedModifierDesc
                    val check = inspection == null || inspection.checkGeneratedModifierDescriptionsForDefinitions
                    val name = ParadoxModifierHandler.getModifierDescKey(modifierName)
                    for(locale in locales) {
                        ProgressManager.checkCanceled()
                        val missing = isMissing(name, project, definition, locale)
                        val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, check, missing, false)
                        codeInsightInfos += codeInsightInfo
                    }
                }
            }
            
            return codeInsightInfos
        }
        
        fun fromModifier(
            element: ParadoxScriptStringExpressionElement,
            locales: Set<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection? = null
        ): List<ParadoxLocalisationCodeInsightInfo>? {
            if(!(inspection == null || inspection.checkForModifiers)) return null
            val modifierName = element.value
            if(modifierName.isEmpty() || modifierName.isParameterized()) return null
            val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return null
            if(config.expression.type != CwtDataType.Modifier) return null
            val project = config.info.configGroup.project
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            
            run {
                val type = Type.ModifierName
                val check = inspection == null || inspection.checkModifierNames
                val name = ParadoxModifierHandler.getModifierNameKey(modifierName)
                for(locale in locales) {
                    ProgressManager.checkCanceled()
                    val missing = isMissing(name, project, element, locale)
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            run {
                val type = Type.ModifierDesc
                val check = inspection == null || inspection.checkModifierDescriptions
                val name = ParadoxModifierHandler.getModifierDescKey(modifierName)
                for(locale in locales) {
                    ProgressManager.checkCanceled()
                    val missing = isMissing(name, project, element, locale)
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            
            return codeInsightInfos
        }
        
        private fun isMissing(name: String, project: Project, context: PsiElement, locale: CwtLocalisationLocaleConfig): Boolean {
            val selector = localisationSelector(project, context).locale(locale).withConstraint(ParadoxLocalisationConstraint.Modifier)
            val localisation = ParadoxLocalisationSearch.search(name, selector).findFirst()
            val missing = localisation == null
            return missing
        }
    }
}
