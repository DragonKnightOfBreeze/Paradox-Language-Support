package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.toCommaDelimitedString
import icu.windea.pls.core.toCommaDelimitedStringList
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression
import javax.swing.JComponent

/**
 * 无法解析的路径引用的代码检查。
 *
 * @property ignoredFileNames （配置项）需要忽略解析的文件名。一组模式，分号分隔，忽略大小写。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class UnresolvedPathReferenceInspection : LocalInspectionTool() {
    @JvmField var ignoredFileNames = "*.lua;*.tga"
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var ignoredByConfigs = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!ChronicleFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 判断是否需要忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.getInlineScriptExpression(file) != null) return false
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file, injectable = !ignoredInInjectedFiles)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val configGroup = ChronicleFacade.getConfigGroup(project, selectGameType(file))
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                if (!element.isDataExpression()) return
                val text = element.text
                if (text.isParameterized()) return // skip if expression is parameterized
                val valueConfig = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return // match or single
                if (isIgnoredByConfigs(element, valueConfig)) return
                val configExpression = valueConfig.configExpression
                val location = element
                if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
                    val filePath = element.value
                    val virtualFile = filePath.toVirtualFile()
                    if (virtualFile != null) return
                    val description = getDescription(configExpression, filePath)
                    holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    return
                }
                val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
                if (pathReferenceExpressionSupport != null) {
                    val pathReference = element.value.normalizePath()
                    run {
                        val fileNames = pathReferenceExpressionSupport.resolveFileName(configExpression, pathReference)
                        if (fileNames.isNullOrEmpty()) return@run
                        if (fileNames.any { fileName -> fileName.matchesPatterns(ignoredFileNames, ignoreCase = true) }) return // 忽略
                    }
                    val selector = ParadoxFilePathSearch.selector(project, file) // use file as context
                    if (ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null) return
                    val description = getDescription(configExpression, pathReference)
                    holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                }
            }

            private fun isIgnoredByConfigs(element: ParadoxScriptStringExpressionElement, memberConfig: CwtMemberConfig<*>): Boolean {
                if (!ignoredByConfigs) return false
                val value = element.value
                val configExpression = memberConfig.configExpression
                if (configExpression != ParadoxInlineScriptManager.inlineScriptPathExpression) {
                    val config = configGroup.extendedInlineScripts.findByPattern(value, element, configGroup)
                    if (config != null) return true
                }
                return false
            }

            private fun getDescription(configExpression: CwtDataExpression, pathReference: String): String {
                return when(configExpression.type) {
                    CwtDataTypes.Icon -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.icon", pathReference, configExpression)
                    CwtDataTypes.FilePath -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.filePath", pathReference, configExpression)
                    CwtDataTypes.FileName -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.fileName", pathReference, configExpression)
                    CwtDataTypes.AbsoluteFilePath -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.abs", pathReference)
                    else -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc", pathReference, configExpression)
                }
            }
        }
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label(ChronicleBundle.message("inspection.script.unresolvedPathReference.option.ignoredFileNames"))
                expandableTextField({ it.toCommaDelimitedStringList() }, { it.toCommaDelimitedString() })
                    .bindText(::ignoredFileNames.toAtomicProperty())
                    .comment(ChronicleBundle.message("comment.patterns"))
                    .align(Align.FILL)
                    .resizableColumn()
            }
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredInInlineScriptFiles
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles"))
                    .bindSelected(::ignoredInInlineScriptFiles.toAtomicProperty())
            }
            // ignoredByConfigs
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs.toAtomicProperty())
            }
        }
    }
}
