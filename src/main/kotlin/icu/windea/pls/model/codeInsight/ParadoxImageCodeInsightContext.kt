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
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

data class ParadoxImageCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxImageCodeInsightInfo>,
    val children: List<ParadoxImageCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        Unresolved
    }
    
    companion object {
        private fun getMissingImageInspection(context: PsiElement): MissingImageInspection? {
            return getInspectionToolState("ParadoxScriptMissingImage", context, context.project)?.enabledTool?.castOrNull()
        }
        
        fun fromFile(
            file: PsiFile,
            fromInspection: Boolean = false,
        ): ParadoxImageCodeInsightContext? {
            if(file !is ParadoxScriptFile) return null
            val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
            val children = mutableListOf<ParadoxImageCodeInsightContext>()
            file.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    when(element) {
                        is ParadoxScriptDefinitionElement -> fromDefinition(element, fromInspection)?.let { children.add(it) }
                        is ParadoxScriptStringExpressionElement -> fromExpression(element, fromInspection)?.let { children.add(it) }
                    }
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
            })
            return ParadoxImageCodeInsightContext(Type.File, file.name, codeInsightInfos, children)
        }
        
        fun fromDefinition(
            definition: ParadoxScriptDefinitionElement,
            fromInspection: Boolean = false,
        ): ParadoxImageCodeInsightContext? {
            val inspection = if(fromInspection) getMissingImageInspection(definition) else null
            
            if(!(inspection == null || inspection.checkForDefinitions)) return null
            val definitionInfo = definition.definitionInfo ?: return null
            val project = definitionInfo.project
            val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
            
            for(info in definitionInfo.images) {
                ProgressManager.checkCanceled()
                val expression = info.locationExpression
                val resolved = expression.resolve(definition, definitionInfo, project)
                val type = when {
                    info.required -> ParadoxImageCodeInsightInfo.Type.Required
                    info.primary -> ParadoxImageCodeInsightInfo.Type.Primary
                    else -> ParadoxImageCodeInsightInfo.Type.Optional
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
                    val type = ParadoxImageCodeInsightInfo.Type.GeneratedModifierIcon
                    val check = inspection == null || inspection.checkGeneratedModifierIconsForDefinitions
                    val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                    val missing = isMissing(iconPath, project, definition)
                    val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
            }
            
            return ParadoxImageCodeInsightContext(Type.Definition, definitionInfo.name, codeInsightInfos, fromInspection = fromInspection)
        }
        
        fun fromExpression(
            element: ParadoxScriptStringExpressionElement,
            fromInspection: Boolean = false,
        ): ParadoxImageCodeInsightContext? {
            val expression = element.value
            if(expression.isEmpty() || expression.isParameterized()) return null
            val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return null
            fromModifier(element, config, fromInspection)?.let { return it }
            return null
        }
        
        fun fromModifier(
            element: ParadoxScriptStringExpressionElement,
            config: CwtMemberConfig<*>,
            fromInspection: Boolean = false,
        ): ParadoxImageCodeInsightContext? {
            val inspection = if(fromInspection) getMissingImageInspection(element) else null
            
            if(!(inspection == null || inspection.checkForModifiers)) return null
            if(config.expression.type != CwtDataType.Modifier) return null
            val modifierName = element.value
            val project = config.info.configGroup.project
            val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
            
            run {
                val type = ParadoxImageCodeInsightInfo.Type.ModifierIcon
                val check = inspection == null || inspection.checkModifierIcons
                val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                val missing = isMissing(iconPath, project, element)
                val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                codeInsightInfos += codeInsightInfo
            }
            
            return ParadoxImageCodeInsightContext(Type.Modifier, modifierName, codeInsightInfos, fromInspection = fromInspection)
        }
        
        private fun isMissing(iconPath: String, project: Project, context: PsiElement): Boolean {
            val iconSelector = fileSelector(project, context)
            val iconFile = ParadoxFilePathSearch.search(iconPath, null, iconSelector).findFirst()
            val missing = iconFile == null
            return missing
        }
    }
}