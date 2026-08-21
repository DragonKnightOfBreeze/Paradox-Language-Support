package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxPsiElementVisitor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

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
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.expandableString("ignoredFileNames", ChronicleBundle.message("inspection.option.ignoredFileNames"), ",")
                .description(ChronicleBundle.message("comment.patterns")),
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles")),
            OptPane.checkbox("ignoredByConfigs", ChronicleBundle.message("inspection.option.ignoredByConfigs")),
            OptPane.checkbox("showExpect", ChronicleBundle.message("inspection.option.showExpect")),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 按需忽略内联脚本文件
        if (ignoredInInlineScriptFiles && ParadoxInlineScriptManager.isInlineScriptFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxPsiElementVisitor() {
            override fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(element: ParadoxScriptStringExpressionElement, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return // skip if is not a data expression
        if (element.text.isParameterized()) return // skip if is parameterized

        // 得到匹配的第一个规则
        val valueConfig = ParadoxConfigManager.getConfigs(element).firstOrNull() ?: return
        val value = element.value
        if (skip(value, element, valueConfig)) return
        val configExpression = valueConfig.configExpression
        val location = element
        if (configExpression.type == CwtDataTypes.AbsoluteFilePath) {
            val virtualFile = value.toVirtualFile()
            if (virtualFile != null) return
            reportProblem(location, value, configExpression, holder)
            return
        }
        val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression.type)
        if (pathReferenceExpressionSupport != null) {
            val pathReference = value.normalizePath()
            run {
                val fileNames = pathReferenceExpressionSupport.resolveFileName(configExpression, pathReference)
                if (fileNames.isNullOrEmpty()) return@run
                if (fileNames.any { fileName -> fileName.matchesPatterns(ignoredFileNames, ignoreCase = true) }) return // 忽略
            }
            val selector = ParadoxFilePathSearch.selector(holder.project, holder.file) // use file as context
            if (ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null) return
            reportProblem(location, value, configExpression, holder)
        }
    }

    private fun skip(filePath: String, element: ParadoxScriptStringExpressionElement, memberConfig: CwtMemberConfig<*>): Boolean {
        if (ignoredByConfigs && ParadoxConfigManager.checkExtendedConfig(filePath, element, memberConfig)) return true
        return false
    }

    private fun reportProblem(location: ParadoxScriptStringExpressionElement, value: String, configExpression: CwtDataExpression, holder: ProblemsHolder) {
        val shortDescription = when (configExpression.type) {
            CwtDataTypes.Icon -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.icon", value)
            CwtDataTypes.FilePath -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.filePath", value)
            CwtDataTypes.FileName -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.fileName", value)
            CwtDataTypes.AbsoluteFilePath -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc.abs", value)
            else -> ChronicleBundle.message("inspection.script.unresolvedPathReference.desc", value)
        }
        val description = when {
            showExpect -> {
                val details = ChronicleBundle.message("inspection.script.unresolvedPathReference.details", configExpression)
                ChronicleBundle.inspectionDescription(shortDescription, details)
            }
            else -> shortDescription
        }
        holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }
}
