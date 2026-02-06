package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightContextBuilder
import icu.windea.pls.model.codeInsight.ParadoxImageCodeInsightInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import javax.swing.JComponent

/**
 * 缺失的图片的代码检查。
 *
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
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
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的脚本文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxDefinitionElement -> visitDefinitionElement(element)
                    is ParadoxScriptStringExpressionElement -> visitStringExpressionElement(element)
                }
            }

            private fun visitDefinitionElement(definition: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()
                val context = ParadoxImageCodeInsightContextBuilder.fromDefinition(definition, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(holder, definition, context)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val context = ParadoxImageCodeInsightContextBuilder.fromExpression(element, fromInspection = true)
                if (context == null || context.infos.isEmpty()) return
                registerProblems(holder, element, context)
            }

            private fun registerProblems(holder: ProblemsHolder, element: PsiElement, context: ParadoxImageCodeInsightContext) {
                val location = when {
                    element is ParadoxScriptFile -> element
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return
                }
                val descriptions = getDescriptions(context)
                if (descriptions.isEmpty()) return
                val fixes = getFixes(element, context)
                for (description in descriptions) {
                    holder.registerProblem(location, description, *fixes)
                }
            }
        }
    }

    private fun getDescriptions(context: ParadoxImageCodeInsightContext): List<String> {
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
        return includeMap.values.mapNotNull { getDescription(it) }
    }

    private fun getDescription(codeInsightInfo: ParadoxImageCodeInsightInfo): String? {
        val locationExpression = codeInsightInfo.relatedImageInfo?.locationExpression
        locationExpression?.takeUnless { it.isPlaceholder }?.location
            ?.let { return PlsBundle.message("inspection.script.missingImage.desc.3", it) }
        codeInsightInfo.gfxName
            ?.let { return PlsBundle.message("inspection.script.missingImage.desc.2", it) }
        codeInsightInfo.filePath
            ?.let { return PlsBundle.message("inspection.script.missingImage.desc.1", it) }
        return null
    }

    @Suppress("unused")
    private fun getFixes(element: PsiElement, context: ParadoxImageCodeInsightContext): Array<LocalQuickFix> {
        // nothing now
        return LocalQuickFix.EMPTY_ARRAY
    }

    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            // checkForDefinitions
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions.toAtomicProperty())
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
                        .bindSelected(::checkPrimaryForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkOptionalForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkGeneratedModifierIconsForDefinitions
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkGeneratedModifierIconsForDefinitions"))
                        .bindSelected(::checkGeneratedModifierIconsForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            // checkForModifiers
            row {
                checkBox(PlsBundle.message("inspection.script.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers.toAtomicProperty())
                    .also { checkForModifiersCb = it }
            }
            indent {
                // checkModifierIcons
                row {
                    checkBox(PlsBundle.message("inspection.script.missingImage.option.checkModifierIcons"))
                        .bindSelected(::checkModifierIcons.toAtomicProperty())
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
