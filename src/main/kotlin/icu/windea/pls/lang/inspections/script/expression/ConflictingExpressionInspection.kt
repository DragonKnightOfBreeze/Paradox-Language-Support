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
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.containingDirectConfig
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.findChild
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptVisitor
import icu.windea.pls.script.psi.isDataExpression
import icu.windea.pls.script.psi.parentProperty

/**
 * 表达式的解析结果存在冲突的代码检查。
 *
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredInInlineScriptFiles （配置项）是否在内联脚本文件中忽略此代码检查。
 */
class ConflictingExpressionInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredInInlineScriptFiles = false

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleBundle.message("inspection.option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredInInlineScriptFiles", ChronicleBundle.message("inspection.option.ignoredInInlineScriptFiles")),
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
            override fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                check(element, holder)
            }
        }
    }

    // NOTE 3.0.2 由于匹配逻辑和检查逻辑存在一些细节上的缺陷，改为默认禁用，避免误报和误导
    // TODO 3.0.2+ 考虑进一步完善相关的匹配逻辑和检查逻辑

    private fun check(element: ParadoxScriptBlock, holder: ProblemsHolder) {
        if (!element.isDataExpression()) return // skip if is not a data expression
        // skip checking property if its property key may contain parameters
        // position: (in property) property key / (standalone) left curly brace
        val property = element.parentProperty
        val position = property?.propertyKey
            ?.also { if (it.text.isParameterized()) return }
            ?: element.findChild { it.elementType == ParadoxScriptElementTypes.LEFT_BRACE }
            ?: return
        val text = property?.presentableText ?: element.presentableText
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        check(element, position, configs, text, holder)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun check(element: ParadoxScriptMember, position: PsiElement, configs: List<CwtMemberConfig<*>>, text: String, holder: ProblemsHolder) {
        if (skip(element, configs)) return
        val isKey = position is ParadoxScriptPropertyKey
        val description = when {
            isKey -> ChronicleBundle.message("inspection.script.conflictingExpression.desc.1", text)
            else -> ChronicleBundle.message("inspection.script.conflictingExpression.desc.2", text)
        }
        holder.registerProblem(position, description)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun skip(element: ParadoxScriptMember, configs: List<CwtMemberConfig<*>>): Boolean {
        // 子句可以精确匹配多个子句规则时，适用此检查
        if (configs.isEmpty()) return true
        // 这里需要先按实际对应的规则位置去重
        if (configs.distinctBy { it.pointer }.size == 1) return true
        // 如果是重载后提供的规则，跳过此检查
        if (isOverriddenConfigs(configs)) return true
        // 如果存在规则，规则的子句中的所有 key 和 value 都可以分别被另一个规则的子句中的所有 key 和 value 包含，则仅使用这些规则
        val configsToCheck = filterConfigs(configs)
        if (configsToCheck.size == 1) return true
        return false
    }

    private fun isOverriddenConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        return configs.any { it.containingDirectConfig.castOrNull<CwtPropertyConfig>()?.overriddenProvider != null }
    }

    private fun filterConfigs(configs: List<CwtMemberConfig<*>>): List<CwtMemberConfig<*>> {
        val configsToCheck = configs.filterFast { config ->
            val childConfigs = config.configs
            childConfigs != null && configs.anyFast { config0 ->
                val childConfigs0 = config0.configs
                config0 != config && childConfigs0 != null && childConfigs0.containsAll(childConfigs)
            }
        }
        return configsToCheck.ifEmpty { configs }
    }
}
