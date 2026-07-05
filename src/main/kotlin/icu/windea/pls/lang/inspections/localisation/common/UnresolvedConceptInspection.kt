package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import javax.swing.JComponent

/**
 * 无法解析的概念的代码检查。
 *
 * @property ignoredByConfigs （配置项）如果对应的扩展的规则存在，是否需要忽略此代码检查。
 * @property ignoredInInjectedFiles （配置项）是否在注入的文件（如，参数值、Markdown 代码块）中忽略此代码检查。
 */
class UnresolvedConceptInspection : LocalInspectionTool() {
    @JvmField var ignoredInInjectedFiles = false
    @JvmField var ignoredByConfigs = false

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 按需忽略注入的文件
        val vFile = file.virtualFile
        if (ignoredInInjectedFiles && VirtualFileService.isInjectedFile(vFile)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val configGroup = ChronicleFacade.getConfigGroup(holder.project, selectGameType(holder.file))
        return object : ParadoxLocalisationVisitor() {
            override fun visitConceptCommand(element: ParadoxLocalisationConceptCommand) {
                ProgressManager.checkCanceled()
                if (isIgnoredByConfigs(element, configGroup)) return
                val name = element.name
                val reference = element.reference
                if (reference == null || reference.resolve() != null) return
                val location = element.conceptName ?: return
                val description = ChronicleBundle.message("inspection.localisation.unresolvedConcept.desc", name)
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
            // ignoredInInjectedFile
            row {
                checkBox(ChronicleBundle.message("inspection.option.ignoredInInjectedFiles"))
                    .bindSelected(::ignoredInInjectedFiles.toAtomicProperty())
            }
            // ignoredByConfigs
            row {
                checkBox(ChronicleBundle.message("inspection.localisation.unresolvedConcept.option.ignoredByConfigs"))
                    .bindSelected(::ignoredByConfigs.toAtomicProperty())
            }
        }
    }
}
