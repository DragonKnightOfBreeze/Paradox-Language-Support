package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

data class ParadoxImageCodeInsightInfo(
    val type: Type,
    val filePath: String?,
    val gfxName: String?,
    val relatedImageInfo: ParadoxDefinitionRelatedImageInfo?,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierIcon,
        ModifierIcon
    }
    
    val key = when {
        relatedImageInfo != null -> "@${relatedImageInfo.key}"
        filePath != null -> filePath
        gfxName != null -> "#$gfxName"
        else -> null
    }
    
    companion object {
        fun fromDefinition(
            definition: ParadoxScriptDefinitionElement,
            inspection: MissingImageInspection? = null
        ): List<ParadoxImageCodeInsightInfo>? {
            if(!(inspection == null || inspection.checkForDefinitions)) return null
            val definitionInfo = definition.definitionInfo ?: return null
            val project = definitionInfo.project
            val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
            
            for(info in definitionInfo.images) {
                ProgressManager.checkCanceled()
                val expression = info.locationExpression
                val resolved = expression.resolve(definition, definitionInfo, project)
                val type = when {
                    info.required -> Type.Required
                    info.primary -> Type.Primary
                    else -> Type.Optional
                }
                val name = resolved?.filePath
                val gfxName = expression.resolvePlaceholder(definitionInfo.name)?.takeIf { it.startsWith("GFX_") }
                val check = when {
                    info.required -> true
                    (inspection == null || inspection.checkPrimaryForDefinitions) && (info.primary || info.primaryByInference) -> true
                    (inspection == null || inspection.checkOptionalForDefinitions) && !info.required -> true
                    else -> false
                }
                val missing = resolved?.file == null && resolved?.message == null
                val dynamic = resolved?.message != null
                val codeInsightInfo = ParadoxImageCodeInsightInfo(type, name, gfxName, info, check, missing, dynamic)
                codeInsightInfos += codeInsightInfo
            }
            
            for(info in definitionInfo.modifiers) {
                ProgressManager.checkCanceled()
                val modifierName = info.name
                run {
                    val type = Type.GeneratedModifierIcon
                    val check =  inspection == null || inspection.checkGeneratedModifierIconsForDefinitions
                    val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                    val missing = isMissing(iconPath, project, definition)
                    val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            
            return codeInsightInfos
        }
        
        fun fromModifier(
            element: ParadoxScriptStringExpressionElement,
            inspection: MissingImageInspection? = null
        ): List<ParadoxImageCodeInsightInfo>? {
            if(!(inspection == null || inspection.checkForModifiers)) return null
            val modifierName = element.value
            if(modifierName.isEmpty() || modifierName.isParameterized()) return null
            val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return null
            if(config.expression.type != CwtDataType.Modifier) return null
            val project = config.info.configGroup.project
            val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
            
            run {
                val type = Type.ModifierIcon
                val check = inspection == null || inspection.checkModifierIcons
                val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                val missing = isMissing(iconPath, project, element)
                val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                codeInsightInfos += codeInsightInfo
            }
            
            return codeInsightInfos
        }
        
        private fun isMissing(iconPath: String, project: Project, context: PsiElement): Boolean {
            val iconSelector = fileSelector(project, context)
            val iconFile = ParadoxFilePathSearch.search(iconPath, null, iconSelector).findFirst()
            val missing = iconFile == null
            return missing
        }
    }
}
