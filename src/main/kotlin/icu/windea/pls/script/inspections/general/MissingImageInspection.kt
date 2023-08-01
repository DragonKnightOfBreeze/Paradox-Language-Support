package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 * @property checkForDefinitions 是否检查定义。默认为true。
 * @property checkPrimaryForDefinitions 是否同样检查定义的主要的相关图片，默认为true。
 * @property checkOptionalForDefinitions 是否同样检查定义的可选的相关图片，默认为false。
 * @property checkForModifiers 是否检查修正（的图标）。默认为false。
 */
class MissingImageInspection : LocalInspectionTool() {
    @JvmField var checkForDefinitions = true
    @JvmField var checkPrimaryForDefinitions = false
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkGeneratedModifierIconsForDefinitions = true
    @JvmField var checkForModifiers = true
    @JvmField var checkModifierIcons = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val file = holder.file
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when(element) {
                    is ParadoxScriptDefinitionElement -> {
                        if(!checkForDefinitions) return
                        val definitionInfo = element.definitionInfo ?: return
                        visitDefinition(element, definitionInfo)
                    }
                    is ParadoxScriptStringExpressionElement -> {
                        if(!checkForModifiers) return
                        visitStringExpressionElement(element)
                    }
                }
            }
            
            private fun visitDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo) {
                val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
                val location = if(definition is ParadoxScriptProperty) definition.propertyKey else definition
                
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
                        checkPrimaryForDefinitions && (info.primary || info.primaryByInference) -> true
                        checkOptionalForDefinitions && !info.required -> true
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
                        val check = checkGeneratedModifierIconsForDefinitions
                        val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                        val missing = isMissing(iconPath)
                        val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                        codeInsightInfos += codeInsightInfo
                    }
                }
                
                registerProblems(codeInsightInfos, location, holder)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val modifierName = element.value
                if(modifierName.isEmpty() || modifierName.isParameterized()) return
                val config = ParadoxConfigHandler.getConfigs(element).firstOrNull() ?: return
                if(config.expression.type != CwtDataType.Modifier) return
                val codeInsightInfos = mutableListOf<ParadoxImageCodeInsightInfo>()
                
                run {
                    val type = ParadoxImageCodeInsightInfo.Type.ModifierIcon
                    val check = checkModifierIcons
                    val iconPath = ParadoxModifierHandler.getModifierIconPath(modifierName)
                    val missing = isMissing(iconPath)
                    val codeInsightInfo = ParadoxImageCodeInsightInfo(type, iconPath, null, null, check, missing, false)
                    codeInsightInfos += codeInsightInfo
                }
                
                registerProblems(codeInsightInfos, element, holder)
            }
            
            private fun isMissing(iconPath: String): Boolean {
                val iconSelector = fileSelector(project, file) //use file as context
                val iconFile = ParadoxFilePathSearch.search(iconPath, null, iconSelector).findFirst()
                val missing = iconFile == null
                return missing
            }
            
            private fun registerProblems(codeInsightInfos: List<ParadoxImageCodeInsightInfo>, element: PsiElement, holder: ProblemsHolder) {
                val messages = getMessages(codeInsightInfos)
                if(messages.isEmpty()) return
                for(message in messages) {
                    //显示为WEAK_WARNING
                    holder.registerProblem(element, message, ProblemHighlightType.WEAK_WARNING)
                }
            }
            
            private fun getMessages(codeInsightInfos: List<ParadoxImageCodeInsightInfo>): List<String> {
                val includeMap = mutableMapOf<String, ParadoxImageCodeInsightInfo>()
                val excludeKeys = mutableSetOf<String>()
                for(codeInsightInfo in codeInsightInfos) {
                    if(!codeInsightInfo.check) continue
                    val key = codeInsightInfo.key ?: continue
                    if(excludeKeys.contains(key)) continue
                    if(codeInsightInfo.missing) {
                        includeMap.putIfAbsent(key, codeInsightInfo)
                    } else {
                        includeMap.remove(key)
                        excludeKeys.add(key)
                    }
                }
                return includeMap.values.mapNotNull { getMessage(it) }
            }
            
            private fun getMessage(codeInsightInfo: ParadoxImageCodeInsightInfo): String? {
                val locationExpression = codeInsightInfo.relatedImageInfo?.locationExpression
                val from = locationExpression?.propertyName?.let { PlsBundle.message("inspection.script.general.missingImage.from.3", it) }
                    ?: codeInsightInfo.gfxName?.let { PlsBundle.message("inspection.script.general.missingImage.from.2", it) }
                    ?: codeInsightInfo.filePath?.let { PlsBundle.message("inspection.script.general.missingImage.from.1", it) }
                    ?: return null
                return PlsBundle.message("inspection.script.general.missingImage.description", from)
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            //checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                //checkRequiredForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                //checkPrimaryForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                //checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions)
                        .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                //checkGeneratedModifierIconsForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkGeneratedModifierIconsForDefinitions"))
                        .bindSelected(::checkGeneratedModifierIconsForDefinitions)
                        .actionListener { _, component -> checkGeneratedModifierIconsForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            //checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
                    .also { checkForModifiersCb = it }
            }
            indent {
                //checkModifierIcons
                row {
                    checkBox(PlsBundle.message("inspection.script.general.missingImage.option.checkModifierIcons"))
                        .bindSelected(::checkModifierIcons)
                        .actionListener { _, component -> checkModifierIcons = component.isSelected }
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
        }
    }
    
    data class Context(
        val info: ParadoxDefinitionRelatedImageInfo,
        val key: String?
    )
}
