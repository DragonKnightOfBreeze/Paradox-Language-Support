package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import javax.swing.JComponent

/**
 * 缺失的图片的代码检查。
 */
class MissingImageInspection : LocalInspectionTool() {
    @JvmField
    var checkForDefinitions = true
    @JvmField
    var checkPrimaryForDefinitions = true
    @JvmField
    var checkOptionalForDefinitions = false
    @JvmField
    var checkGeneratedModifierIconsForDefinitions = false
    @JvmField
    var checkForModifiers = false
    @JvmField
    var checkModifierIcons = true

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxScriptDefinitionElement -> visitDefinition(element)
                    is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                }
            }

            private fun visitDefinition(definition: ParadoxScriptDefinitionElement) {
                val context = ParadoxImageCodeInsightContextBuilder.fromDefinition(definition, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(context, definition, holder)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                val context = ParadoxImageCodeInsightContextBuilder.fromExpression(element, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
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
                if (messages.isEmpty()) return
                val fixes = getFixes(element, context)
                for (message in messages) {
                    holder.registerProblem(location, message, *fixes)
                }
            }

            private fun getMessages(context: ParadoxImageCodeInsightContext): List<String> {
                val includeMap = mutableMapOf<String, ParadoxImageCodeInsightInfo>()
                val excludeKeys = mutableSetOf<String>()
                for (codeInsightInfo in context.infos) {
                    if (!codeInsightInfo.check) continue
                    val key = codeInsightInfo.key ?: continue
                    if (excludeKeys.contains(key)) continue
                    if (codeInsightInfo.missing) {
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
                val fromProperty = locationExpression?.takeUnless { it.isPlaceholder }?.location
                val from = fromProperty?.let { PlsBundle.message("inspection.script.missingImage.from.3", it) }
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
            // checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions)
                    .actionListener { _, component -> checkForDefinitions = component.isSelected }
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                // checkRequiredForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                // checkPrimaryForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions)
                        .actionListener { _, component -> checkPrimaryForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions)
                        .actionListener { _, component -> checkOptionalForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkGeneratedModifierIconsForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkGeneratedModifierIconsForDefinitions"))
                        .bindSelected(::checkGeneratedModifierIconsForDefinitions)
                        .actionListener { _, component -> checkGeneratedModifierIconsForDefinitions = component.isSelected }
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            // checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers)
                    .actionListener { _, component -> checkForModifiers = component.isSelected }
                    .also { checkForModifiersCb = it }
            }
            indent {
                // checkModifierIcons
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkModifierIcons"))
                        .bindSelected(::checkModifierIcons)
                        .actionListener { _, component -> checkModifierIcons = component.isSelected }
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
        }
    }

    @Suppress("unused")
    private fun getFixes(element: PsiElement, context: ParadoxImageCodeInsightContext): Array<LocalQuickFix> {
        // nothing now
        return LocalQuickFix.EMPTY_ARRAY
    }
}
