package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 无法解析的路径引用的检查。
 *
 * @property ignoredFileNames （配置项）需要忽略的文件名的模式。使用GLOB模式。忽略大小写。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles 是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedPathReferenceInspection : LocalInspectionTool() {
    @JvmField
    var ignoredByConfigs = false
    @JvmField
    var ignoredFileNames = "*.lua;*.tga"
    @JvmField
    var ignoredInInjectedFiles = false
    @JvmField
    var ignoredInInlineScriptFiles = false

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (ignoredInInjectedFiles && PlsFileManager.isInjectedFile(holder.file.virtualFile)) return PsiElementVisitor.EMPTY_VISITOR
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(holder.file) != null) return PsiElementVisitor.EMPTY_VISITOR

        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val file = holder.file
        val project = holder.project
        val configGroup = PlsFacade.getConfigGroup(project, selectGameType(file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val text = element.text
                if (text.isParameterized()) return //skip if expression is parameterized
                val valueConfig = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return //match or single
                if (isIgnoredByConfigs(element, valueConfig)) return
                val configExpression = valueConfig.configExpression
                val location = element
                if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
                    val filePath = element.value
                    val virtualFile = filePath.toVirtualFile(false)
                    if (virtualFile != null) return
                    val message = PlsBundle.message("inspection.script.unresolvedPathReference.desc.abs", filePath)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    return
                }
                val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
                if (pathReferenceExpressionSupport != null) {
                    val pathReference = element.value.normalizePath()
                    run {
                        val fileNames = pathReferenceExpressionSupport.resolveFileName(configExpression, pathReference)
                        if (fileNames.isNullOrEmpty()) return@run
                        ignoredFileNames.splitOptimized(';').forEach {
                            if (fileNames.any { fileName -> fileName.matchesPattern(it, true) }) return
                        }
                    }
                    val selector = selector(project, file).file() //use file as context
                    if (ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null) return
                    val message = pathReferenceExpressionSupport.getUnresolvedMessage(configExpression, pathReference)
                    holder.registerProblem(location, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                }
            }

            private fun isIgnoredByConfigs(element: ParadoxScriptStringExpressionElement, memberConfig: CwtMemberConfig<*>): Boolean {
                if (!ignoredByConfigs) return false
                val value = element.value
                val configExpression = memberConfig.configExpression
                if (configExpression.expressionString != ParadoxInlineScriptManager.inlineScriptPathExpressionString) {
                    val config = configGroup.extendedInlineScripts.findFromPattern(value, element, configGroup)
                    if (config != null) return true
                }
                return false
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            //ignoredByConfigs
            row {
                checkBox(PlsBundle.message("inspection.script.unresolvedExpression.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs)
                    .actionListener { _, component -> ignoredByConfigs = component.isSelected }
            }
            row {
                label(PlsBundle.message("inspection.script.unresolvedPathReference.option.ignoredFileNames"))
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.unresolvedPathReference.option.ignoredFileNames.tooltip") }
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFileNames)
                    .bindTextWhenChanged(::ignoredFileNames)
                    .comment(PlsBundle.message("inspection.script.unresolvedPathReference.option.ignoredFileNames.comment"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
            //ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles)
                    .actionListener { _, component -> ignoredInInjectedFiles = component.isSelected }
            }
            //ignoredInInlineScriptFiles
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles)
                    .actionListener { _, component -> ignoredInInlineScriptFiles = component.isSelected }
            }
        }
    }
}
