package icu.windea.pls.lang.inspections.script.expression

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.findChild
import icu.windea.pls.core.inspections.InspectionService
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOccurrence
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptVisitor
import icu.windea.pls.script.psi.isDataExpression
import icu.windea.pls.script.psi.parentProperty

/**
 * 缺失的表达式的代码检查。
 *
 * @property firstOnly （配置项）是否仅标出第一个错误。
 * @property firstOnlyOnFile （配置项）在文件级别上，是否仅标出第一个错误。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class MissingExpressionInspection : LocalInspectionTool() {
    @JvmField var firstOnly = false
    @JvmField var firstOnlyOnFile = true
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false
    @JvmField var showExpect = true

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("firstOnly", ChronicleBundle.message("inspection.script.missingExpression.option.firstOnly")),
            OptPane.checkbox("firstOnlyOnFile", ChronicleBundle.message("inspection.script.missingExpression.option.firstOnlyOnFile")),
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles")),
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
        return object : ParadoxScriptVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is ParadoxScriptFile) return
                ProgressManager.checkCanceled()
                check(file, holder)
            }

            override fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    private fun check(file: ParadoxScriptFile, holder: ProblemsHolder) {
        val configContext = ParadoxConfigManager.getConfigContext(file) ?: return
        if (configContext.skipMissingExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(file, ParadoxMatchOptions(forDeclarationRoot = true))
        check(file, file, configs, holder)
    }

    private fun check(element: ParadoxScriptBlock, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return // skip check if element is not an expression
        // skip checking property if its property key may contain parameters
        // position: (in property) property key / (standalone) left curly brace
        val property = element.parentProperty
        val position = property?.propertyKey
            ?.also { if (it.text.isParameterized()) return }
            ?: element.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE }
            ?: return
        val configContext = ParadoxConfigManager.getConfigContext(element) ?: return
        if (configContext.skipMissingExpressionCheck()) return
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        check(element, position, configs, holder)
    }

    private fun check(element: ParadoxScriptMember, position: PsiElement, configs: List<CwtMemberConfig<*>>, holder: ProblemsHolder) {
        if (skip(element, configs)) return
        val occurrences = ParadoxConfigManager.getChildOccurrences(element, configs)
        if (occurrences.isEmpty()) return
        val overriddenProvider = ParadoxConfigManager.getOverriddenProvider(configs)
        occurrences.forEach { (configExpression, occurrence) ->
            if (overriddenProvider != null && overriddenProvider.skipMissingExpressionCheck(configs, configExpression)) return@forEach
            val r = checkOccurrence(element, position, occurrence, configExpression, holder)
            if (!r) return
        }
    }

    private fun skip(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
        // 子句不为空且可以精确匹配多个子句规则时，不适用此检查
        return when {
            configs.isEmpty() -> true
            configs.size == 1 -> false
            element is ParadoxScriptFile && element.members().none() -> false
            element is ParadoxScriptBlock && element.members().none() -> false
            else -> true
        }
    }

    private fun checkOccurrence(element: ParadoxScriptMember, position: PsiElement, occurrence: ParadoxMatchOccurrence, configExpression: CwtDataExpression, holder: ProblemsHolder): Boolean {
        val (actual, min, _, lenientMin) = occurrence
        if (min != null && actual < min) {
            val expressionType = ChronicleBundle.expressionType(configExpression)
            val isConst = configExpression.type == CwtDataTypes.Constant
            val shortDescription = when {
                isConst -> ChronicleBundle.message("inspection.script.missingExpression.desc.1", expressionType, configExpression)
                else -> ChronicleBundle.message("inspection.script.missingExpression.desc.2", expressionType, configExpression)
            }
            val description = when {
                showExpect -> {
                    val minDefine = occurrence.minDefine
                    val details = when {
                        minDefine == null -> ChronicleBundle.message("inspection.script.missingExpression.details.1", min, actual)
                        else -> ChronicleBundle.message("inspection.script.missingExpression.details.2", min, actual, minDefine)
                    }
                    ChronicleBundle.inspectionDescription(shortDescription, details)
                }
                else -> shortDescription
            }
            val highlightType = InspectionService.getWeakerHighlightType(lenientMin)
            val fileLevel = element is PsiFile
            if (!fileLevel && firstOnly && holder.hasResults()) return false
            if (fileLevel && firstOnlyOnFile && holder.hasResults()) return false
            holder.registerProblem(position, description, highlightType)
        }
        return true
    }
}
