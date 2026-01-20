package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import javax.swing.JComponent

/**
 * 无法解析的概念的代码检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的CWT规则存在，是否需要忽略此代码检查。
 * @property ignoredInInjectedFiles 是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedConceptInspection : LocalInspectionTool() {
    @JvmField
    var ignoredByConfigs = false
    @JvmField
    var ignoredInInjectedFiles = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求规则分组数据已加载完毕
        if (!PlsFacade.checkConfigGroupInitialized(file.project, file)) return false
        // 要求是符合条件的本地化文件
        val injectable = !ignoredInInjectedFiles
        return ParadoxPsiFileMatcher.isLocalisationFile(file, smart = true, injectable = injectable)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = PlsFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxLocalisationVisitor() {
            override fun visitConceptCommand(element: ParadoxLocalisationConceptCommand) {
                ProgressManager.checkCanceled()
                if (isIgnoredByConfigs(element, configGroup)) return
                val name = element.name
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.conceptName ?: return
                val description = PlsBundle.message("inspection.localisation.unresolvedConcept.desc", name)
                holder.registerProblem(location, description, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            }
        }
    }

    private fun isIgnoredByConfigs(element: ParadoxLocalisationConceptCommand, configGroup: CwtConfigGroup): Boolean {
        if (!ignoredByConfigs) return false
        val name = element.name
        val configs = configGroup.extendedDefinitions.findByPattern(name, element, configGroup).orEmpty()
        val config = configs.find { ParadoxDefinitionTypeExpression.resolve(it.type).matches(ParadoxDefinitionTypes.gameConcept) }
        if (config != null) return true
        return false
    }

    override fun createOptionsPanel(): JComponent {
        return panel {
            // ignoredByConfigs
            row {
                checkBox(PlsBundle.message("inspection.localisation.unresolvedConcept.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs.toAtomicProperty())
            }
            // ignoredInInjectedFile
            row {
                checkBox(PlsBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
        }
    }
}
