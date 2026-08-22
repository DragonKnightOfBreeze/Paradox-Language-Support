package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.options.OptPane
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.annotations.ForGameTypeConstraint
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.matchesPatterns
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.inspections.ChronicleInspectionBundle
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextFormat
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.ParadoxSyntaxConstraint

/**
 * 无法解析的文本格式的代码检查。
 *
 * @property ignoredNames （配置项）需要忽略的名字。一组模式，分号分隔，忽略大小写。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 * @property ignoredByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 */
@ForGameTypeConstraint(ParadoxGameTypeConstraint.JominiBased)
class UnresolvedTextFormatInspection : LocalInspectionTool() {
    object Constants {
        const val definitionType = ParadoxDefinitionTypes.textFormat
        // aka predefined format styles, or color expressions, or combined
        const val defaultIgnoredNames = "bold;semibold;extrabold;italic;underline;strikethrough;indent_newline;tooltip:"
    }

    @JvmField var ignoredNames = Constants.defaultIgnoredNames
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredByConfigs = false

    override fun getOptionsPane(): OptPane {
        return OptPane.pane(
            OptPane.checkbox("ignoredNames", ChronicleInspectionBundle.message("localisation.unresolvedTextFormat.option.ignoredNames")),
            OptPane.checkbox("ignoredInInjectedFiles", ChronicleInspectionBundle.message("option.ignoredInInjectedFiles")),
            OptPane.checkbox("ignoredByConfigs", ChronicleInspectionBundle.message("option.ignoredByConfigs")),
        )
    }

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求游戏类型支持文本格式
        if (!ParadoxSyntaxConstraint.LocalisationTextFormat.testTarget(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxLocalisationVisitor() {
            override fun visitTextFormat(element: ParadoxLocalisationTextFormat) {
                ProgressManager.checkCanceled()
                check(element, configGroup, holder)
            }
        }
    }

    private fun check(element: ParadoxLocalisationTextFormat, configGroup: CwtConfigGroup, holder: ProblemsHolder) {
        val name = element.name ?: return
        if (skip(name, element, configGroup)) return // 忽略
        val reference = element.reference
        if (reference == null || reference.resolve() != null) return
        val location = element.idElement ?: return
        val description = ChronicleInspectionBundle.message("localisation.unresolvedTextFormat.desc", name)
        holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }

    private fun skip(name: String, element: ParadoxLocalisationTextFormat, configGroup: CwtConfigGroup): Boolean {
        if (ignoredNames.isNotEmpty() && name.matchesPatterns(ignoredNames, ignoreCase = true)) return true
        if (ignoredByConfigs && ParadoxConfigManager.checkExtendedConfig(name, Constants.definitionType, element, configGroup)) return true
        return false
    }
}
