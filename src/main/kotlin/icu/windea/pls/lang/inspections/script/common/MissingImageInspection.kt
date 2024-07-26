package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 缺失的图片的检查。
 */
class MissingImageInspection : LocalInspectionTool() {
    @JvmField var checkForDefinitions = true
    @JvmField var checkPrimaryForDefinitions = false
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkGeneratedModifierIconsForDefinitions = false
    @JvmField var checkForModifiers = true
    @JvmField var checkModifierIcons = true
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when(element) {
                    is ParadoxScriptDefinitionElement -> visitDefinition(element)
                    is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                }
            }
            
            private fun visitDefinition(definition: ParadoxScriptDefinitionElement) {
                val context = ParadoxImageCodeInsightContext.fromDefinition(definition, fromInspection = true)
                if(context == null || context.infos.isEmpty()) return
                registerProblems(context, definition, holder)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val context = ParadoxImageCodeInsightContext.fromExpression(element, fromInspection = true)
                if(context == null || context.infos.isEmpty()) return
                registerProblems(context, element, holder)
            }
            
            private fun registerProblems(context: ParadoxImageCodeInsightContext, element: PsiElement, holder: ProblemsHolder) {
                val location = when {
                    element is ParadoxScriptFile -> element
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return
                }
                val messages = getMessages(context)
                if(messages.isEmpty()) return
                for(message in messages) {
                    //显示为WEAK_WARNING
                    holder.registerProblem(location, message, ProblemHighlightType.WEAK_WARNING)
                }
            }
            
            private fun getMessages(context: ParadoxImageCodeInsightContext): List<String> {
                val includeMap = mutableMapOf<String, ParadoxImageCodeInsightInfo>()
                val excludeKeys = mutableSetOf<String>()
                for(codeInsightInfo in context.infos) {
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
                val from = locationExpression?.propertyName?.let { PlsBundle.message("inspection.script.missingImage.from.3", it) }
                    ?: codeInsightInfo.gfxName?.let { PlsBundle.message("inspection.script.missingImage.from.2", it) }
                    ?: codeInsightInfo.filePath?.let { PlsBundle.message("inspection.script.missingImage.from.1", it) }
                    ?: return null
                return PlsBundle.message("inspection.script.missingImage.desc", from)
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            //checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                //checkRequiredForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                //checkPrimaryForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                //checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions)
                        .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                //checkGeneratedModifierIconsForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkGeneratedModifierIconsForDefinitions"))
                        .bindSelected(::checkGeneratedModifierIconsForDefinitions)
                        .actionListener { _, component -> checkGeneratedModifierIconsForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            //checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
                    .also { checkForModifiersCb = it }
            }
            indent {
                //checkModifierIcons
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkModifierIcons"))
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
