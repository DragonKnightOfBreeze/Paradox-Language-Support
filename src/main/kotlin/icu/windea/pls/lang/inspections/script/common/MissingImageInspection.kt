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
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.codeInsight.ParadoxImageCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxImageCodeInsightContextFactory
import icu.windea.pls.lang.codeInsight.ParadoxImageCodeInsightInfo
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression
import javax.swing.JComponent

/**
 * 检查是否存在缺失的图片。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class MissingImageInspection : LocalInspectionTool() {
    @JvmField var checkForDefinitions = true
    @JvmField var checkPrimaryForDefinitions = true
    @JvmField var checkOptionalForDefinitions = false
    @JvmField var checkGeneratedModifierIconsForDefinitions = false
    @JvmField var checkForModifiers = false
    @JvmField var checkModifierIcons = true
    @JvmField var ignoredInInjectedFiles = false

    override fun createOptionsPanel(): JComponent {
        lateinit var checkForDefinitionsCb: Cell<JBCheckBox>
        lateinit var checkForModifiersCb: Cell<JBCheckBox>
        return panel {
            // checkForDefinitions
            row {
                checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkForDefinitions"))
                    .bindSelected(::checkForDefinitions.toAtomicProperty())
                    .also { checkForDefinitionsCb = it }
            }
            indent {
                // checkRequiredForDefinitions
                row {
                    checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkRequiredForDefinitions"))
                        .selected(true)
                        .enabled(false)
                }
                // checkPrimaryForDefinitions
                row {
                    checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkPrimaryForDefinitions"))
                        .bindSelected(::checkPrimaryForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkOptionalForDefinitions
                row {
                    checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkOptionalForDefinitions"))
                        .bindSelected(::checkOptionalForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
                // checkGeneratedModifierIconsForDefinitions
                row {
                    checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkGeneratedModifierIconsForDefinitions"))
                        .bindSelected(::checkGeneratedModifierIconsForDefinitions.toAtomicProperty())
                        .enabledIf(checkForDefinitionsCb.selected)
                }
            }
            // checkForModifiers
            row {
                checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkForModifiers"))
                    .bindSelected(::checkForModifiers.toAtomicProperty())
                    .also { checkForModifiersCb = it }
            }
            indent {
                // checkModifierIcons
                row {
                    checkBox(ChronicleInspectionBundle.message("script.missingImage.option.checkModifierIcons"))
                        .bindSelected(::checkModifierIcons.toAtomicProperty())
                        .enabledIf(checkForModifiersCb.selected)
                }
            }
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleInspectionBundle.message("option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxPsiElementVisitor() {
            override fun visitDefinitionElement(element: ParadoxDefinitionElement) {
                ProgressManager.checkCanceled()
                checkFromDefinition(element, holder)
            }

            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return
                checkFromExpression(element, holder)
            }
        }
    }

    private fun checkFromDefinition(element: ParadoxDefinitionElement, holder: ProblemsHolder) {
        val context = ParadoxImageCodeInsightContextFactory.fromDefinition(element, fromInspection = true)
        if (context == null || context.infos.isEmpty()) return
        registerProblems(element, context, holder)
    }

    private fun checkFromExpression(element: ParadoxScriptStringExpressionElement, holder: ProblemsHolder) {
        val context = ParadoxImageCodeInsightContextFactory.fromExpression(element, fromInspection = true)
        if (context == null || context.infos.isEmpty()) return
        registerProblems(element, context, holder)
    }

    private fun registerProblems(element: PsiElement, context: ParadoxImageCodeInsightContext, holder: ProblemsHolder) {
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

    private fun getDescriptions(context: ParadoxImageCodeInsightContext): List<String> {
        val includeMap = mutableMapOf<String, ParadoxImageCodeInsightInfo>()
        val excludeKeys = mutableSetOf<String>()
        context.infos.forEachFast f@{ codeInsightInfo ->
            if (!codeInsightInfo.check) return@f
            val key = codeInsightInfo.key ?: return@f
            if (excludeKeys.contains(key)) return@f
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
            ?.let { return ChronicleInspectionBundle.message("script.missingImage.desc.3", it) }
        codeInsightInfo.gfxName
            ?.let { return ChronicleInspectionBundle.message("script.missingImage.desc.2", it) }
        codeInsightInfo.filePath
            ?.let { return ChronicleInspectionBundle.message("script.missingImage.desc.1", it) }
        return null
    }

    @Suppress("unused")
    private fun getFixes(element: PsiElement, context: ParadoxImageCodeInsightContext): Array<LocalQuickFix> {
        // nothing now
        return LocalQuickFix.EMPTY_ARRAY
    }
}
