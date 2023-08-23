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
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

data class ParadoxLocalisationCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxLocalisationCodeInsightInfo>,
    val children: List<ParadoxLocalisationCodeInsightContext> = emptyList(),
    val inspection: MissingLocalisationInspection? = null
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        LocalisationReference,
        SyncedLocalisationReference
    }
    
    companion object {
        fun fromFile(
            file: PsiFile,
            locales: Collection<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection? = null
        ): ParadoxLocalisationCodeInsightContext? {
            if(file !is ParadoxScriptFile) return null
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            val children = mutableListOf<ParadoxLocalisationCodeInsightContext>()
            file.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    when(element) {
                        is ParadoxScriptDefinitionElement -> fromDefinition(element, locales, inspection)?.let { children.add(it) }
                        is ParadoxScriptStringExpressionElement -> fromExpression(element, locales, inspection)?.let { children.add(it) }
                    }
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
            //exclude duplicates and sort contexts
            val finalChildren = children
                .distinctBy { it.type.name + "@" + it.name }
                .sortedWith(compareBy( { it.type }, { if(it.type == Type.LocalisationReference || it.type == Type.SyncedLocalisationReference) it.name else 0 }))
            return ParadoxLocalisationCodeInsightContext(Type.File, file.name, codeInsightInfos, finalChildren, inspection)
        }
        
        fun fromDefinition(
            definition: ParadoxScriptDefinitionElement,
            locales: Collection<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection? = null
        ): ParadoxLocalisationCodeInsightContext? {
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
                    val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierName
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
                    val type = ParadoxLocalisationCodeInsightInfo.Type.GeneratedModifierDesc
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
            
            return ParadoxLocalisationCodeInsightContext(Type.Definition, definitionInfo.name, codeInsightInfos, inspection = inspection)
        }
        
        fun fromExpression(
            element: ParadoxScriptStringExpressionElement,
            locales: Collection<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection? = null
        ): ParadoxLocalisationCodeInsightContext? {
            if(!(inspection == null || inspection.checkForModifiers)) return null //quick check
            if(!element.isExpression()) return null
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return null
            val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return null
            fromModifier(element, config, locales, inspection)?.let { return it }
            fromReference(element, config, locales)?.let{ return it }
            return null
        }
        
        fun fromModifier(
            element: ParadoxScriptStringExpressionElement,
            config: CwtMemberConfig<*>,
            locales: Collection<CwtLocalisationLocaleConfig>,
            inspection: MissingLocalisationInspection?
        ): ParadoxLocalisationCodeInsightContext? {
            if(!(inspection == null || inspection.checkForModifiers)) return null
            if(config.expression.type != CwtDataType.Modifier) return null
            val modifierName = element.value
            val project = config.info.configGroup.project
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            
            run {
                val type = ParadoxLocalisationCodeInsightInfo.Type.ModifierName
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
                val type = ParadoxLocalisationCodeInsightInfo.Type.ModifierDesc
                val check = inspection == null || inspection.checkModifierDescriptions
                val name = ParadoxModifierHandler.getModifierDescKey(modifierName)
                for(locale in locales) {
                    ProgressManager.checkCanceled()
                    val missing = isMissing(name, project, element, locale)
                    val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            
            return ParadoxLocalisationCodeInsightContext(Type.Modifier, modifierName, codeInsightInfos, inspection = inspection)
        }
        
        fun fromReference(
            element: ParadoxScriptStringExpressionElement,
            config: CwtMemberConfig<*>,
            locales: Collection<CwtLocalisationLocaleConfig>,
            unresolved: Boolean = false
        ): ParadoxLocalisationCodeInsightContext? {
            val contextType = when {
                config.expression.type == CwtDataType.Localisation -> Type.LocalisationReference
                config.expression.type == CwtDataType.SyncedLocalisation -> Type.SyncedLocalisationReference
                config.expression.type == CwtDataType.InlineLocalisation && !element.text.isLeftQuoted() -> Type.LocalisationReference
                else -> null
            }
            if(contextType == null) return null
            val type = ParadoxLocalisationCodeInsightInfo.Type.Reference
            val name = element.value
            if(name.isEmpty()) return null
            val codeInsightInfos = mutableListOf<ParadoxLocalisationCodeInsightInfo>()
            val project by lazy { element.project }
            for(locale in locales) {
                ProgressManager.checkCanceled()
                val missing = if(unresolved) true else isMissing(name, project, element, locale)
                val codeInsightInfo = ParadoxLocalisationCodeInsightInfo(type, name, null, locale, true, missing, false)
                codeInsightInfos += codeInsightInfo
            }
            return ParadoxLocalisationCodeInsightContext(contextType, name, codeInsightInfos)
        }
        
        private fun isMissing(name: String, project: Project, context: PsiElement, locale: CwtLocalisationLocaleConfig): Boolean {
            val selector = localisationSelector(project, context).locale(locale).withConstraint(ParadoxLocalisationConstraint.Modifier)
            val localisation = ParadoxLocalisationSearch.search(name, selector).findFirst()
            val missing = localisation == null
            return missing
        }
    }
}